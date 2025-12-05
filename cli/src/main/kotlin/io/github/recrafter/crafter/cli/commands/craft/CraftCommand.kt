package io.github.recrafter.crafter.cli.commands.craft

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor.CYAN
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor.RED
import io.github.recrafter.crafter.cli.bash.ansi.AnsiStyle.BOLD
import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.conditions.BashConditions.isPidAlive
import io.github.recrafter.crafter.cli.bash.conditions.not_
import io.github.recrafter.crafter.cli.bash.properties.function
import io.github.recrafter.crafter.cli.bash.properties.stringVar
import io.github.recrafter.crafter.cli.bash.utils.Cmd
import io.github.recrafter.crafter.cli.bash.variables.*
import io.github.recrafter.crafter.cli.commands.init.InitCommand
import io.github.recrafter.crafter.cli.commands.process.CraftProcess
import io.github.recrafter.crafter.cli.commands.process.GradleProcessCommand
import io.github.recrafter.crafter.cli.commands.process.Spinner
import io.github.recrafter.crafter.cli.extensions.common.Builder
import io.github.recrafter.crafter.cli.extensions.common.script
import io.github.recrafter.crafter.cli.extensions.common.spaced
import io.github.recrafter.crafter.cli.extensions.common.withScript

@CLICommand(name = "craft", description = "Build the mod and launch the selected Minecraft side")
object CraftCommand : GradleProcessCommand<CraftArguments>(CraftArguments.serializer()) {

    override fun getCompletions(argumentName: String, arguments: CraftArguments): String = withScript {
        when (argumentName) {
            arguments::loader.name -> bash.getString("LOADERS").value
            arguments::version.name -> bash.getMap("VERSIONS").getValue(arguments.loader)
            else -> failWithInvalidValue(argumentName)
        }
    }

    override fun run(fingerprint: Fingerprint, arguments: CraftArguments): String = script {
        val modProjectName by stringVar()
        detectModProjectName(arguments.loader, arguments.version, allowRangeSwitch = true) { name, isRange ->
            setStringValue(modProjectName, name)
            if (isRange) {
                break_()
            }
            return@detectModProjectName this
        }
        ifBlock {
            if_(modProjectName.isEmpty()) {
                val displayedVersion = buildString {
                    append(bash.getMap("LOADER_DISPLAY_NAMES").getValue(arguments.loader))
                    append(Constants.Char.SPACE)
                    append(arguments.version)
                }
                print_("The mod for ", RED)
                print_(displayedVersion, RED, BOLD)
                print_(" is not initialized yet.", RED)
                println_()
                println_("You need to initialize it before crafting.")
                println_("Run the following command:")
                println_()
                withPadding {
                    println_(
                        Cmd.of(fingerprint.scriptName, spaced(InitCommand.name, arguments.loader, arguments.version)),
                        CYAN
                    )
                }
                println_()
                error_()
            }
        }
        val spinner = Spinner.build(this)
        cursor.hide()
        when_(arguments.side.value) {
            case_(CraftSide.CLIENT.getName()) {
                val process = CraftProcess.build(this, ModSide.CLIENT, false)
                onExit {
                    cursor.show()
                    ensureProcessKilled(process.pid)
                    ensureProcessKilled(process.logWatcher.pid)
                }
                craftSingleSide(process, arguments, modProjectName, spinner)
            }.case_(CraftSide.SERVER.getName()) {
                val process = CraftProcess.build(this, ModSide.SERVER, false)
                onExit {
                    cursor.show()
                    ensureProcessKilled(process.pid)
                    ensureProcessKilled(process.logWatcher.pid)
                }
                craftSingleSide(process, arguments, modProjectName, spinner)
            }.case_(CraftSide.LAUNCHER.getName()) {
                craftLauncher(this, arguments.loader, arguments.version, modProjectName, spinner)
            }
        }
    }

    private fun ScriptBuilder.craftSingleSide(
        process: CraftProcess,
        arguments: CraftArguments,
        modProjectName: StringVar,
        spinner: Spinner,
    ): ScriptBuilder {
        onInterrupt {
            process.interrupt(this)
        }
        runTask(arguments.loader, arguments.version, modProjectName, process)
        process.runLogWatcher(this)
        loop {
            ifBlock {
                if_(process.isRunning.equals_(false)) {
                    checkVarUpdate(process.runningStateSync)
                }
            }
            onNextVar(process.logWatcher.queue) {
                clearLine()
                process.printLogLine(this)
                println_()
            }
            process.printStatus(this, spinner)
            ifBlock {
                if_(bash.conditions.isPidAlive(process.pid).not_()) {
                    break_()
                }
            }
        }
        return this
    }

    fun craftLauncher(
        builder: ScriptBuilder,
        loader: StringVar,
        version: StringVar,
        modProjectName: StringVar,
        spinner: Spinner,
        onInterrupted: Builder<ScriptBuilder> = { this },
    ): ScriptBuilder = builder.run {
        val serverProcess = CraftProcess.build(this, ModSide.SERVER, true)
        val clientProcess = CraftProcess.build(this, ModSide.CLIENT, true)
        onExit {
            cursor.show()
            ensureProcessKilled(serverProcess.pid)
            ensureProcessKilled(serverProcess.logWatcher.pid)

            ensureProcessKilled(clientProcess.pid)
            ensureProcessKilled(clientProcess.logWatcher.pid)
        }
        onInterrupt {
            ifBlock {
                if_(bash.conditions.isPidAlive(clientProcess.pid)) {
                    cursor.moveDown(2)
                    cursor.moveUp()
                }
            }
            serverProcess.interrupt(this)
            cursor.moveDown()
            clientProcess.interrupt(this)
            onInterrupted()
        }
        runTask(loader, version, modProjectName, serverProcess)
        serverProcess.runLogWatcher(this)
        val runClientFunction by function {
            runTask(loader, version, modProjectName, clientProcess)
            clientProcess.runLogWatcher(this)
        }
        loop {
            ifBlock {
                if_(serverProcess.isRunning.equals_(false)) {
                    checkVarUpdate(serverProcess.runningStateSync)
                    ifBlock {
                        if_(serverProcess.isRunning.equals_(true)) {
                            callFunction(runClientFunction)
                        }
                    }
                }.if_(clientProcess.isRunning.equals_(false)) {
                    checkVarUpdate(clientProcess.runningStateSync)
                }
            }
            ifBlock {
                if_(serverProcess.isRunning.equals_(true)) {
                    cursor.moveUp()
                    onNextVar(clientProcess.logWatcher.queue) {
                        clearLine()
                        clientProcess.printLogLine(this)
                        cursor.moveDown()
                    }
                }
            }
            onNextVar(serverProcess.logWatcher.queue) {
                clearLine()
                serverProcess.printLogLine(this)
                cursor.moveDown()
            }
            serverProcess.printStatus(this, spinner)
            ifBlock {
                if_(serverProcess.isRunning.equals_(true)) {
                    println_()
                    clientProcess.printStatus(this, spinner)
                }
            }
        }
    }
}
