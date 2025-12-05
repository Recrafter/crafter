package io.github.recrafter.crafter.cli.commands.craft

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
import io.github.recrafter.crafter.cli.bash.properties.*
import io.github.recrafter.crafter.cli.bash.utils.Cmd
import io.github.recrafter.crafter.cli.bash.variables.*
import io.github.recrafter.crafter.cli.commands.init.InitCommand
import io.github.recrafter.crafter.cli.extensions.angled
import io.github.recrafter.crafter.cli.extensions.common.script
import io.github.recrafter.crafter.cli.extensions.common.withScript
import io.github.recrafter.crafter.cli.extensions.rounded
import io.github.recrafter.crafter.cli.extensions.squared
import io.github.recrafter.crafter.core.helpers.server.ServerCommands
import io.github.recrafter.crafter.core.tasks.public.CraftClientTask
import io.github.recrafter.crafter.core.tasks.public.CraftServerTask

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
                    println_(Cmd.of(fingerprint.scriptName, spaced(InitCommand.name, loader, version)), CYAN)
                }
                println_()
                throw_()
            }
        }
        val spinnerSequence by stringVar(Spinners.DOTS)
        val spinnerProgress by intVar()
        val spinnerLength by intVar(spinnerSequence.length)
        val spinner = Spinner(spinnerSequence, spinnerLength, spinnerProgress)

        cursor.hide()
        when_(arguments.side.value) {
            case_(CraftSide.CLIENT.getName()) {
                val clientGradlePid by stringVar()
                val isClientRunning by booleanVar()
                val clientExitCode by intVar()
                val clientLogPath by stringVar()
                val clientLogLine by stringVar()
                val clientLogWatcherPid by stringVar()
                runSide(
                    side = ModSide.CLIENT,
                    arguments = arguments,
                    spinner = spinner,
                    modProjectName = modProjectName,
                    gradlePid = clientGradlePid,
                    isRunning = isClientRunning,
                    isRunningSync = initVarSync(isClientRunning),
                    exitCode = clientExitCode,
                    logPath = clientLogPath,
                    logLine = clientLogLine,
                    logQueue = initVarSync(clientLogLine, VarSyncStrategy.QUEUE),
                    logWatcherPid = clientLogWatcherPid,
                )
            }.case_(CraftSide.SERVER.getName()) {
                val serverGradlePid by stringVar()
                val isServerRunning by booleanVar()
                val serverExitCode by intVar()
                val serverLogPath by stringVar()
                val serverLogLine by stringVar()
                val serverLogWatcherPid by stringVar()
                val serverCommandInput by commandInput()
                runSide(
                    side = ModSide.SERVER,
                    arguments = arguments,
                    spinner = spinner,
                    modProjectName = modProjectName,
                    gradlePid = serverGradlePid,
                    isRunning = isServerRunning,
                    isRunningSync = initVarSync(isServerRunning),
                    exitCode = serverExitCode,
                    logPath = serverLogPath,
                    logLine = serverLogLine,
                    logQueue = initVarSync(serverLogLine, VarSyncStrategy.QUEUE),
                    logWatcherPid = serverLogWatcherPid,
                    commandInput = serverCommandInput,
                )
            }.case_(CraftSide.MERGED.getName()) {
                val serverGradlePid by stringVar()
                val isServerRunning by booleanVar()
                val isServerRunningSync = initVarSync(isServerRunning)
                val serverExitCode by intVar()
                val serverLogPath by stringVar()
                val serverLogLine by stringVar()
                val serverLogQueue = initVarSync(serverLogLine, VarSyncStrategy.QUEUE)
                val serverLogWatcherPid by stringVar()
                val serverInput by commandInput()

                val clientGradlePid by stringVar()
                val isClientRunning by booleanVar()
                val isClientRunningSync = initVarSync(isClientRunning)
                val clientExitCode by intVar()
                val clientLogPath by stringVar()
                val clientLogLine by stringVar()
                val clientLogQueue = initVarSync(clientLogLine, VarSyncStrategy.QUEUE)
                val clientLogWatcherPid by stringVar()

                val runClientFunction by function {
                    runGradleTaskInBackground(
                        CraftClientTask::class, loader, version, modProjectName, clientGradlePid, clientLogPath
                    )
                    runSideLogWatcher(
                        ModSide.CLIENT,
                        clientGradlePid,
                        isClientRunning,
                        isClientRunningSync,
                        clientLogPath,
                        clientLogQueue,
                        clientLogWatcherPid,
                    )
                }

                onInterrupt {
                    ifBlock {
                        if_(bash.conditions.isPidAlive(clientGradlePid)) {
                            cursor.moveDown()
                            cursor.moveDown()
                            cursor.moveUp()
                        }
                    }
                    interruptSide(
                        ModSide.SERVER, arguments, serverGradlePid, isServerRunning, serverLogWatcherPid, serverInput
                    )
                    cursor.moveDown()
                    interruptSide(ModSide.CLIENT, arguments, clientGradlePid, isClientRunning, clientLogWatcherPid)
                }
                runGradleTaskInBackground(
                    taskClass = CraftServerTask::class,
                    loader = loader,
                    version = version,
                    modProjectName = modProjectName,
                    pid = serverGradlePid,
                    logPath = serverLogPath,
                    commandInput = serverInput,
                )
                runSideLogWatcher(
                    side = ModSide.SERVER,
                    gradlePid = serverGradlePid,
                    isRunning = isServerRunning,
                    isRunningSync = isServerRunningSync,
                    logPath = serverLogPath,
                    logQueue = serverLogQueue,
                    logWatcherPid = serverLogWatcherPid,
                )
                loop {
                    ifBlock {
                        if_(isServerRunning.equals_(false)) {
                            checkVarUpdate(isServerRunningSync)
                            ifBlock {
                                if_(isServerRunning.equals_(true)) {
                                    callFunction(runClientFunction)
                                }
                            }
                        }.if_(isClientRunning.equals_(false)) {
                            checkVarUpdate(isClientRunningSync)
                        }
                    }
                    ifBlock {
                        if_(isServerRunning.equals_(true)) {
                            cursor.moveUp()
                            onNextVar(clientLogQueue) {
                                clearLine()
                                print_(ModSide.CLIENT.name.squared(), ModSide.CLIENT.getMergedLogColor())
                                print_(spaces(1))
                                print_(clientLogLine)
                                cursor.moveDown()
                            }
                        }
                    }
                    onNextVar(serverLogQueue) {
                        clearLine()
                        print_(ModSide.SERVER.name.squared(), ModSide.SERVER.getMergedLogColor())
                        print_(spaces(1))
                        print_(serverLogLine)
                        cursor.moveDown()
                    }
                    printSideStatus(
                        ModSide.SERVER,
                        arguments,
                        spinner,
                        serverGradlePid,
                        isServerRunning,
                        serverExitCode,
                        serverLogPath
                    )
                    ifBlock {
                        if_(isServerRunning.equals_(true)) {
                            println_()
                            printSideStatus(
                                ModSide.CLIENT,
                                arguments,
                                spinner,
                                clientGradlePid,
                                isClientRunning,
                                clientExitCode,
                                clientLogPath
                            )
                        }
                    }
                }
            }
        }
    }

    private fun ScriptBuilder.runSide(
        side: ModSide, arguments: CraftArguments, spinner: Spinner, modProjectName: StringVar,
        gradlePid: StringVar, isRunning: BooleanVar, isRunningSync: VarSync, exitCode: IntVar,
        logPath: StringVar, logLine: StringVar, logQueue: VarSync, logWatcherPid: StringVar,
        commandInput: CommandInputReference? = null,
    ): ScriptBuilder {
        onInterrupt {
            interruptSide(side, arguments, gradlePid, isRunning, logWatcherPid, commandInput)
        }
        runGradleTaskInBackground(
            taskClass = when (side) {
                ModSide.CLIENT -> CraftClientTask::class
                ModSide.SERVER -> CraftServerTask::class
            },
            loader = arguments.loader,
            version = arguments.version,
            modProjectName = modProjectName,
            pid = gradlePid,
            logPath = logPath,
            commandInput = commandInput,
        )
        runSideLogWatcher(side, gradlePid, isRunning, isRunningSync, logPath, logQueue, logWatcherPid)
        loop {
            ifBlock {
                if_(isRunning.equals_(false)) {
                    checkVarUpdate(isRunningSync)
                }
            }
            onNextVar(logQueue) {
                clearLine()
                println_(logLine)
            }
            printSideStatus(side, arguments, spinner, gradlePid, isRunning, exitCode, logPath)
        }
        return this
    }

    private fun ScriptBuilder.printSideStatus(
        side: ModSide, arguments: CraftArguments, spinner: Spinner,
        gradlePid: StringVar, isRunning: BooleanVar, exitCode: IntVar,
        logPath: StringVar,
    ): ScriptBuilder {
        ifBlock {
            if_(bash.conditions.isPidAlive(gradlePid)) {
                clearLine()
                ifBlock {
                    val versionToDisplay = getVersionToDisplay(arguments)
                    if_(isRunning.equals_(false)) {
                        val spinnerChar by stringVar(spinner.sequence.getCharAt(spinner.progress.mod(spinner.length)))
                        print_(spinnerChar, CYAN)
                        print_(" Preparing ", CYAN)
                        printSide(side, CYAN)
                        print_(" for ", CYAN)
                        print_(versionToDisplay, CYAN, BOLD)
                        print_("...", CYAN)
                        incrementIntValue(spinner.progress)
                    }.if_(isRunning.equals_(true)) {
                        printSide(side, GREEN)
                        print_(" for ", GREEN)
                        print_(versionToDisplay, GREEN, BOLD)
                        print_(" is running...", GREEN)
                        print_(" (Press ", GRAY)
                        print_(INTERRUPT_KEY.angled(), GRAY, BOLD)
                        print_(" to stop)", GRAY)
                    }
                }
            }.else_ {
                clearLine()
                ifBlock {
                    if_(exitCode.isEmpty()) {
                        setIntValue(exitCode, wait(gradlePid))
                    }
                }
                ifBlock {
                    if_(exitCode.equals_(SUCCESS)) {
                        printSide(side, GRAY)
                        print_(" closed.", GRAY)
                    }.else_ {
                        printSide(side, RED)
                        print_(" crashed. ", RED)
                        print_("See full log at ${bash.getAbsolutePath(logPath.value)}", GRAY)
                    }
                }
            }
        }
        return this
    }

    private fun ScriptBuilder.interruptSide(
        side: ModSide, arguments: CraftArguments,
        gradlePid: StringVar, isRunning: BooleanVar,
        logWatcherPid: StringVar,
        input: CommandInputReference? = null,
    ): ScriptBuilder {
        ensureProcessKilled(logWatcherPid)
        ifBlock {
            if_(bash.conditions.isPidAlive(gradlePid)) {
                if (side == ModSide.SERVER) {
                    ifBlock {
                        if_(isRunning.equals_(false)) {
                            clearLine()
                            print_("Waiting for the server to start so it can be stopped cleanly...", YELLOW)
                        }
                    }
                    input?.let { sendCommand(it, ServerCommands.STOP) }
                } else {
                    kill(gradlePid)
                }
                wait(gradlePid)
                ifBlock {
                    val versionToDisplay = getVersionToDisplay(arguments)
                    if_(isRunning.equals_(false)) {
                        clearLine()
                        print_("Crafting ", RED)
                        printSide(side, RED)
                        print_(" for ", RED)
                        print_(versionToDisplay, RED, BOLD)
                        print_(" was interrupted by user.", RED)
                    }.if_(isRunning.equals_(true)) {
                        clearLine()
                        print_("Stopping ", GRAY)
                        printSide(side, GRAY)
                        print_(" for ", GRAY)
                        print_(versionToDisplay, GRAY, BOLD)
                        print_(" ${INTERRUPT_KEY.rounded()}...", GRAY)
                    }
                }
            }
        }
        return this
    }

    private fun ScriptBuilder.runSideLogWatcher(
        side: ModSide,
        gradlePid: StringVar, isRunning: BooleanVar, isRunningSync: VarSync,
        logPath: StringVar, logQueue: VarSync, logWatcherPid: StringVar,
    ): ScriptBuilder {
        setStringValue(logWatcherPid, watchFileLinesInBackground(logPath, gradlePid) { newLine ->
            ifBlock {
                if_(isRunning.equals_(false)) {
                    ifBlock {
                        val runningIndicators = when (side) {
                            ModSide.CLIENT -> listOf("[Render thread/", "[LWJGL]", "fps,")
                            ModSide.SERVER -> listOf("Done")
                        }
                        ifAny(runningIndicators.map { newLine.contains(it) }) {
                            setBooleanValue(isRunning, true)
                            notifyVarChanged(isRunningSync)
                        }
                    }
                }
            }
            notifyVarChanged(logQueue, newLine.value)
        })
        return this
    }

    private fun ScriptBuilder.getVersionToDisplay(arguments: CraftArguments): String = withScript {
        buildString {
            append(bash.getMap("LOADER_DISPLAY_NAMES").getValue(arguments.loader))
            append(spaces(1))
            append(arguments.version)
        }
    }

    private fun ScriptBuilder.printSide(side: ModSide, color: AnsiColor): ScriptBuilder {
        print_(MinecraftConstants.FULL_GAME_NAME, color)
        print_(" ${side.getName()}", color, BOLD)
        return this
    }

    private fun ModSide.getMergedLogColor(): AnsiColor =
        when (this) {
            ModSide.CLIENT -> MAGENTA
            ModSide.SERVER -> YELLOW
        }

    private class Spinner(
        val sequence: StringVar,
        val length: IntVar,
        val progress: IntVar,
    )
}
