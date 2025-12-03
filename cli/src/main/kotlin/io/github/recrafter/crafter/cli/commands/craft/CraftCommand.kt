package io.github.recrafter.crafter.cli.commands.craft

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.MinecraftConstants
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.bash.ExitCode.SUCCESS
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor.*
import io.github.recrafter.crafter.cli.bash.ansi.AnsiStyle.BOLD
import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.bash.api.commands.AbstractCLICommand
import io.github.recrafter.crafter.cli.bash.ascii.Spinners
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.conditions.BashConditions.isPidAlive
import io.github.recrafter.crafter.cli.bash.conditions.not_
import io.github.recrafter.crafter.cli.bash.properties.enumVar
import io.github.recrafter.crafter.cli.bash.properties.function
import io.github.recrafter.crafter.cli.bash.properties.intVar
import io.github.recrafter.crafter.cli.bash.properties.stringVar
import io.github.recrafter.crafter.cli.bash.utils.Cmd
import io.github.recrafter.crafter.cli.bash.variables.*
import io.github.recrafter.crafter.cli.commands.craft.CraftCommand.CraftingStatus.*
import io.github.recrafter.crafter.cli.commands.init.InitCommand
import io.github.recrafter.crafter.cli.extensions.angled
import io.github.recrafter.crafter.cli.extensions.common.script
import io.github.recrafter.crafter.cli.extensions.common.withScript
import io.github.recrafter.crafter.cli.extensions.rounded
import io.github.recrafter.crafter.cli.extensions.squared
import io.github.recrafter.crafter.core.tasks.public.CraftClientTask
import io.github.recrafter.crafter.core.tasks.public.CraftServerTask
import io.github.recrafter.crafter.tasks.public.InstallCrafterCLITask

@CLICommand(name = "craft", description = "Build the mod and launch the selected Minecraft side")
object CraftCommand : AbstractCLICommand<CraftArguments>(CraftArguments.serializer()) {

    override fun getCompletions(argumentName: String, arguments: CraftArguments): String = withScript {
        when (argumentName) {
            arguments::loader.name -> bash.getString("LOADERS").value
            arguments::version.name -> bash.getMap("VERSIONS").getValue(arguments.loader)
            else -> failWithInvalidValue(argumentName)
        }
    }

    override fun run(fingerprint: Fingerprint, arguments: CraftArguments): String = script {
        val loader = arguments.loader
        val version = arguments.version
        val modProjectName by stringVar()
        findModProject(loader, version, allowRangeSwitch = true) { projectName, isRange ->
            setStringValue(modProjectName, projectName)
            if (isRange) {
                break_()
            }
            return@findModProject this
        }
        ifBlock {
            if_(modProjectName.isEmpty()) {
                print_("The mod for ", RED)
                print_(getVersionToDisplay(arguments), RED, BOLD)
                print_(" is not initialized yet.", RED)
                println_()
                println_("You need to initialize it before crafting.")
                println_("Run the following command:")
                println_()
                withPadding {
                    println_(
                        Cmd.of(fingerprint.scriptName, spaced(InitCommand.name, loader, version)),
                        CYAN
                    )
                }
                println_()
                throw_()
            }
        }
        val logsDirectoryPath by stringVar(InstallCrafterCLITask.CLI_CACHE_DIRECTORY_PATH.appendPath("gradle-output"))
        createDirectory(logsDirectoryPath.value)

        val spinnerSequence by stringVar(Spinners.DOTS)
        val spinnerProgress by intVar()
        val spinnerLength by intVar(spinnerSequence.length)
        val spinner = Spinner(spinnerSequence, spinnerLength, spinnerProgress)

        val serverPid by stringVar()
        val serverStatus by enumVar(PREPARING)
        val serverLogPath by stringVar()
        val serverStatusSync = initVarSync(serverStatus)
        val serverLogLine by stringVar()
        val serverLogQueue = initVarSync(serverLogLine, VarSyncStrategy.QUEUE)
        val serverExitCode by intVar()

        val clientPid by stringVar()
        val clientStatus by enumVar(PREPARING)
        val clientLogPath by stringVar()
        val clientStatusSync = initVarSync(clientStatus)
        val clientLogLine by stringVar()
        val clientLogQueue = initVarSync(clientLogLine, VarSyncStrategy.QUEUE)
        val clientExitCode by intVar()

        cursor.hide()
        when_(arguments.side.value) {
            case_(CraftSideType.CLIENT.getName()) {
                onInterrupt {
                    ifBlock {
                        if_(bash.conditions.isPidAlive(clientPid)) {
                            ifBlock {
                                if_(clientStatus.equals_(RUNNING)) {
                                    setEnumValue(clientStatus, STOPPING)
                                    clearLine()
                                    printSideStatus(ModSide.CLIENT, STOPPING, arguments)
                                    println_()
                                }
                            }
                            kill(clientPid)
                            wait(clientPid)
                            return@if_ this
                        }
                    }
                    clearLine()
                    ifBlock {
                        if_(clientStatus.equals_(PREPARING)) {
                            printSideStatus(ModSide.CLIENT, INTERRUPTED, arguments)
                            println_()
                        }
                    }
                }
                runGradleTask(CraftClientTask::class, loader, version, modProjectName, clientPid, clientLogPath)
                watchFileLines(clientLogPath, clientPid, background = true) { clientLine ->
                    ifBlock {
                        if_(clientStatus.equals_(PREPARING)) {
                            ifBlock {
                                ifAny(
                                    clientLine.contains("[Render thread/"),
                                    clientLine.contains("[LWJGL]"),
                                    clientLine.contains("fps,")
                                ) {
                                    setEnumValue(clientStatus, RUNNING)
                                    notifyVarChanged(clientStatusSync)
                                }
                            }
                        }
                    }
                    notifyVarChanged(clientLogQueue, clientLine.value)
                }
                while_(bash.conditions.isPidAlive(clientPid)) {
                    ifBlock {
                        if_(clientStatus.equals_(PREPARING)) {
                            checkVarUpdate(clientStatusSync)
                        }
                    }
                    onNextVar(clientLogQueue) {
                        clearLine()
                        println_(clientLogLine)
                    }
                    ifBlock {
                        if_(clientStatus.equals_(PREPARING)) {
                            clearLine()
                            printSidePreparing(ModSide.CLIENT, arguments, spinner)
                        }.if_(clientStatus.equals_(RUNNING)) {
                            clearLine()
                            printSideStatus(ModSide.CLIENT, RUNNING, arguments)
                        }
                    }
                }
                setIntValue(clientExitCode, wait(clientPid))
                clearLine()
                ifBlock {
                    if_(clientExitCode.equals_(SUCCESS)) {
                        printSideClosed(ModSide.CLIENT)
                        println_()
                    }.else_ {
                        printSideCrashed(ModSide.CLIENT, clientLogPath)
                        println_()
                        throw_()
                    }
                }
            }.case_(CraftSideType.SERVER.getName()) {
                onInterrupt {
                    ifBlock {
                        if_(bash.conditions.isPidAlive(serverPid)) {
                            ifBlock {
                                if_(serverStatus.equals_(RUNNING)) {
                                    setEnumValue(serverStatus, STOPPING)
                                    clearLine()
                                    printSideStatus(ModSide.SERVER, STOPPING, arguments)
                                    println_()
                                }
                            }
                            kill(serverPid)
                            wait(serverPid)
                            return@if_ this
                        }
                    }
                    clearLine()
                    ifBlock {
                        if_(serverStatus.equals_(PREPARING)) {
                            printSideStatus(ModSide.SERVER, INTERRUPTED, arguments)
                            println_()
                        }
                    }
                }
                runGradleTask(CraftServerTask::class, loader, version, modProjectName, serverPid, serverLogPath)
                watchFileLines(serverLogPath, serverPid, background = true) { serverLine ->
                    ifBlock {
                        ifAll(serverStatus.equals_(PREPARING), serverLine.contains("Done")) {
                            setEnumValue(serverStatus, RUNNING)
                            notifyVarChanged(serverStatusSync)
                        }
                    }
                    notifyVarChanged(serverLogQueue, serverLine.value)
                }
                while_(bash.conditions.isPidAlive(serverPid)) {
                    ifBlock {
                        if_(serverStatus.equals_(PREPARING)) {
                            checkVarUpdate(serverStatusSync)
                        }
                    }
                    onNextVar(serverLogQueue) {
                        clearLine()
                        println_(serverLogLine)
                    }
                    ifBlock {
                        if_(serverStatus.equals_(PREPARING)) {
                            clearLine()
                            printSidePreparing(ModSide.SERVER, arguments, spinner)
                        }.if_(serverStatus.equals_(RUNNING)) {
                            clearLine()
                            printSideStatus(ModSide.SERVER, RUNNING, arguments)
                        }
                    }
                }
                setIntValue(serverExitCode, wait(serverPid))
                clearLine()
                ifBlock {
                    if_(serverExitCode.equals_(SUCCESS).not_()) {
                        printSideCrashed(ModSide.SERVER, serverLogPath)
                        println_()
                        throw_()
                    }
                }
            }.case_(CraftSideType.MERGED.getName()) {
                onInterrupt {
                    ifBlock {
                        if_(bash.conditions.isPidAlive(clientPid)) {
                            ifBlock {
                                if_(clientStatus.equals_(RUNNING)) {
                                    cursor.moveDown()
                                    setEnumValue(clientStatus, STOPPING)
                                    clearLine()
                                    printSideStatus(ModSide.CLIENT, STOPPING, arguments)
                                    cursor.moveUp()
                                }
                            }
                            kill(clientPid)
                            wait(clientPid)
                            return@if_ this
                        }
                    }
                    ifBlock {
                        if_(bash.conditions.isPidAlive(serverPid)) {
                            ifBlock {
                                if_(serverStatus.equals_(RUNNING)) {
                                    setEnumValue(serverStatus, STOPPING)
                                    clearLine()
                                    printSideStatus(ModSide.SERVER, STOPPING, arguments)
                                    println_()
                                }
                            }
                            kill(serverPid)
                            wait(serverPid)
                            return@if_ this
                        }
                    }
                    ifBlock {
                        if_(serverStatus.equals_(PREPARING)) {
                            clearLine()
                            printSideStatus(ModSide.SERVER, INTERRUPTED, arguments)
                        }.if_(clientStatus.equals_(PREPARING)) {
                            clearLine()
                            printSideStatus(ModSide.CLIENT, INTERRUPTED, arguments)
                        }
                    }
                }
                runGradleTask(CraftServerTask::class, loader, version, modProjectName, serverPid, serverLogPath)
                watchFileLines(serverLogPath, serverPid, background = true) { serverLine ->
                    ifBlock {
                        ifAll(clientStatus.equals_(PREPARING), serverLine.contains("Done")) {
                            setEnumValue(serverStatus, RUNNING)
                            notifyVarChanged(serverStatusSync)
                        }
                    }
                    notifyVarChanged(serverLogQueue, serverLine.value)
                }
                val runClientFunction by function {
                    runGradleTask(CraftClientTask::class, loader, version, modProjectName, clientPid, clientLogPath)
                    watchFileLines(clientLogPath, clientPid, background = true) { clientLine ->
                        ifBlock {
                            if_(clientStatus.equals_(PREPARING)) {
                                ifBlock {
                                    ifAny(
                                        clientLine.contains("[Render thread/"),
                                        clientLine.contains("[LWJGL]"),
                                        clientLine.contains("fps,"),
                                    ) {
                                        setEnumValue(clientStatus, RUNNING)
                                        notifyVarChanged(clientStatusSync)
                                    }
                                }
                            }
                        }
                        notifyVarChanged(clientLogQueue, clientLine.value)
                    }
                }
                while_(bash.conditions.isPidAlive(serverPid)) {
                    ifBlock {
                        ifAny(serverStatus.equals_(STOPPING), clientStatus.equals_(STOPPING)) {
                            break_()
                        }
                    }
                    ifBlock {
                        if_(serverStatus.equals_(PREPARING)) {
                            checkVarUpdate(serverStatusSync)
                            ifBlock {
                                if_(serverStatus.equals_(RUNNING)) {
                                    callFunction(runClientFunction)
                                }
                            }
                        }.if_(clientStatus.equals_(PREPARING)) {
                            checkVarUpdate(clientStatusSync)
                        }
                    }
                    ifBlock {
                        if_(serverStatus.equals_(RUNNING)) {
                            cursor.moveUp()
                            onNextVar(clientLogQueue) {
                                clearLine()
                                print_(ModSide.CLIENT.name.squared(), MAGENTA)
                                print_(spaces(1))
                                print_(clientLogLine)
                                cursor.moveDown()
                            }
                        }
                    }
                    onNextVar(serverLogQueue) {
                        clearLine()
                        print_(ModSide.SERVER.name.squared(), YELLOW)
                        print_(spaces(1))
                        print_(serverLogLine)
                        cursor.moveDown()
                    }
                    ifBlock {
                        if_(serverStatus.equals_(PREPARING)) {
                            clearLine()
                            printSidePreparing(ModSide.SERVER, arguments, spinner)
                        }.else_ {
                            ifBlock {
                                if_(serverStatus.equals_(RUNNING)) {
                                    clearLine()
                                    printSideStatus(ModSide.SERVER, RUNNING, arguments)
                                }
                            }
                            ifBlock {
                                if_(bash.conditions.isPidAlive(clientPid)) {
                                    ifBlock {
                                        if_(clientStatus.equals_(PREPARING)) {
                                            println_()
                                            clearLine()
                                            printSidePreparing(ModSide.CLIENT, arguments, spinner)
                                        }.if_(clientStatus.equals_(RUNNING)) {
                                            println_()
                                            clearLine()
                                            printSideStatus(ModSide.CLIENT, RUNNING, arguments)
                                        }
                                    }
                                }.else_ {
                                    ifBlock {
                                        if_(clientExitCode.isEmpty()) {
                                            setIntValue(clientExitCode, wait(clientPid))
                                        }
                                    }
                                    println_()
                                    clearLine()
                                    ifBlock {
                                        if_(clientExitCode.equals_(SUCCESS)) {
                                            printSideClosed(ModSide.CLIENT)
                                        }.else_ {
                                            printSideCrashed(ModSide.CLIENT, clientLogPath)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                setIntValue(serverExitCode, wait(serverPid))
                clearLine()
                ifBlock {
                    if_(serverExitCode.equals_(SUCCESS).not_()) {
                        println_()
                        throw_()
                    }
                }
            }
        }
    }

    private fun getVersionToDisplay(arguments: CraftArguments): String = withScript {
        buildString {
            append(bash.getMap("LOADER_DISPLAY_NAMES").getValue(arguments.loader))
            append(spaces(1))
            append(arguments.version)
        }
    }

    private fun ScriptBuilder.printSideStatus(
        side: ModSide,
        status: CraftingStatus,
        arguments: CraftArguments,
    ): ScriptBuilder {
        val versionToDisplay = getVersionToDisplay(arguments)
        when (status) {
            RUNNING -> {
                printSide(side, GREEN)
                print_(" for ", GREEN)
                print_(versionToDisplay, GREEN, BOLD)
                print_(" is running...", GREEN)
                print_(" (Press ", GRAY)
                print_(INTERRUPT_KEY.angled(), GRAY, BOLD)
                print_(" to stop)", GRAY)
            }

            STOPPING -> {
                print_("Stopping ", GRAY)
                printSide(side, GRAY)
                print_(" for ", GRAY)
                print_(versionToDisplay, GRAY, BOLD)
                print_(" ${INTERRUPT_KEY.rounded()}...", GRAY)
            }

            INTERRUPTED -> {
                print_("Crafting ", RED)
                printSide(side, RED)
                print_(" for ", RED)
                print_(versionToDisplay, RED, BOLD)
                print_(" was interrupted by user.", RED)
            }

            PREPARING -> gradleError("Use printSidePreparing() instead printSideStatus()")
            CLOSED -> gradleError("Use printSideClosed() instead printSideStatus()")
            CRASHED -> gradleError("Use printSideCrashed() instead printSideStatus()")
        }
        return this
    }

    private fun ScriptBuilder.printSidePreparing(
        side: ModSide,
        arguments: CraftArguments,
        spinner: Spinner
    ): ScriptBuilder {
        val spinnerChar by stringVar(spinner.sequence.getCharAt(spinner.progress.mod(spinner.length)))
        print_(spinnerChar, CYAN)
        print_(" Preparing ", CYAN)
        printSide(side, CYAN)
        print_(" for ", CYAN)
        print_(getVersionToDisplay(arguments), CYAN, BOLD)
        print_("...", CYAN)
        incrementIntValue(spinner.progress)
        return this
    }

    private fun ScriptBuilder.printSideClosed(side: ModSide): ScriptBuilder {
        printSide(side, GRAY)
        print_(" closed.", GRAY)
        return this
    }

    private fun ScriptBuilder.printSideCrashed(side: ModSide, logPath: StringVar): ScriptBuilder {
        printSide(side, RED)
        print_(" crashed. ", RED)
        print_("See full log at ${bash.getAbsolutePath(logPath.value)}", GRAY)
        return this
    }

    private fun ScriptBuilder.printSide(side: ModSide, color: AnsiColor): ScriptBuilder {
        print_(MinecraftConstants.FULL_GAME_NAME, color)
        print_(" ${side.getName()}", color, BOLD)
        return this
    }

    private enum class CraftingStatus {
        PREPARING, RUNNING, STOPPING, INTERRUPTED, CLOSED, CRASHED,
    }

    private class Spinner(
        val sequence: StringVar,
        val length: IntVar,
        val progress: IntVar,
    )
}
