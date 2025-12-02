package io.github.recrafter.crafter.cli.commands.craft

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.MinecraftConstants
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MinecraftVersionRange
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.ascii.Spinners
import io.github.recrafter.crafter.cli.bash.builder.*
import io.github.recrafter.crafter.cli.bash.builder.Conditions.isDirectoryExists
import io.github.recrafter.crafter.cli.bash.builder.Conditions.isPidAlive
import io.github.recrafter.crafter.cli.bash.syntax.BashOperator
import io.github.recrafter.crafter.cli.bash.utils.Cmd
import io.github.recrafter.crafter.cli.commands.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.commands.api.common.AbstractCLICommand
import io.github.recrafter.crafter.cli.commands.init.InitCommand
import io.github.recrafter.crafter.cli.extensions.angled
import io.github.recrafter.crafter.cli.extensions.common.bashScript
import io.github.recrafter.crafter.cli.extensions.common.withBashScript
import io.github.recrafter.crafter.cli.extensions.rounded
import io.github.recrafter.crafter.cli.extensions.unquoted
import io.github.recrafter.crafter.cli.properties.arrayVar
import io.github.recrafter.crafter.cli.properties.booleanVar
import io.github.recrafter.crafter.cli.properties.intVar
import io.github.recrafter.crafter.cli.properties.stringVar

@CLICommand(name = "craft", description = "Build the mod and launch the selected Minecraft side for development")
object CraftCommand : AbstractCLICommand<CraftArguments>(CraftArguments.serializer()) {

    override fun getCompletions(argumentName: String, arguments: CraftArguments): String = withBashScript {
        when (argumentName) {
            arguments::loader.name -> bash.getStringVar("LOADERS").quotedValue
            arguments::version.name -> bash.getMapVar("VERSIONS").getValue(arguments.loader)
            else -> failWithInvalidValue(argumentName)
        }
    }

    override fun run(fingerprint: Fingerprint, arguments: CraftArguments): String = bashScript {
        val unquotedLoader = arguments.loader.unquoted()
        val unquotedVersion = arguments.version.unquoted()
        val loaderDisplayName = bash.getMapVar("LOADER_DISPLAY_NAMES").getValue(arguments.loader)
        val displayedVersion = loaderDisplayName + Constants.Char.SPACE + arguments.version
        val modProjectName by stringVar()
        ifBlock {
            if_(bash.conditions.isDirectoryExists(unquotedLoader.appendPath(unquotedVersion))) {
                setStringVarValue(modProjectName, unquotedVersion)
            }.else_ {
                val versionsString by stringVar(bash.getMapVar("VERSIONS").getValue(arguments.loader))
                val versionsArray by arrayVar(versionsString.toString())
                val versionIndices = initArrayIndicesMap(versionsArray)
                val versionIndex by intVar(versionIndices.getValue(arguments.version))
                val versionFiles by arrayVar("${arguments.loader}/*")
                foreach_(versionFiles) {
                    ifBlock {
                        if_(bash.conditions.isDirectoryExists(it.toString()).not_()) {
                            continue_()
                        }
                    }
                    val directoryName by stringVar(it.substringAfterLast(Constants.Char.SLASH))
                    ifBlock {
                        if_(directoryName.matches("^(.+)${MinecraftVersionRange.MOD_PROJECT_NAME_SEPARATOR}(.+)$")) {
                            val min by stringVar(bash.getArrayVar("BASH_REMATCH").getElement(1))
                            val max by stringVar(bash.getArrayVar("BASH_REMATCH").getElement(2))
                            val minIndex by intVar(versionIndices.getValue(min.value))
                            val maxIndex by intVar(versionIndices.getValue(max.value))
                            ifBlock {
                                if_(
                                    listOf(
                                        minIndex.isNotEmpty(),
                                        maxIndex.isNotEmpty(),
                                        minIndex.isLessOrEqual(versionIndex),
                                        versionIndex.isLessOrEqual(maxIndex),
                                    )
                                ) {
                                    ifBlock {
                                        if_(versionIndex.equals_(minIndex).not_()) {
                                            print_("Requested version ", AnsiColor.YELLOW)
                                            print_(unquotedVersion, AnsiColor.YELLOW, AnsiStyle.BOLD)
                                            print_(" does not exist as a standalone mod project. ", AnsiColor.YELLOW)
                                            println_()
                                            print_("Automatically using version range ", AnsiColor.YELLOW)
                                            print_(directoryName, AnsiColor.YELLOW, AnsiStyle.BOLD)
                                            print_(" that includes it.", AnsiColor.YELLOW)
                                            println_()
                                            initStringVar("VERSION", min.toString())
                                            return@if_ this
                                        }
                                    }
                                    setStringVarValue(modProjectName, directoryName.toString())
                                    break_()
                                }
                            }
                        }
                    }
                }
            }
        }
        ifBlock {
            if_(modProjectName.isEmpty()) {
                print_("The mod for ", AnsiColor.RED)
                print_(displayedVersion, AnsiColor.RED, AnsiStyle.BOLD)
                print_(" is not initialized yet.", AnsiColor.RED)
                println_()
                println_("You need to initialize it before crafting.")
                println_("Run the following command:")
                println_()
                withPadding {
                    println_(
                        Cmd.of(fingerprint.scriptName, "${InitCommand.name} $unquotedLoader $unquotedVersion"),
                        AnsiColor.CYAN
                    )
                }
                println_()
                throw_()
            }
        }
        val spinner by stringVar(Spinners.DOTS)
        val spinnerProgress by intVar()
        val spinnerLength by intVar(spinner.length)
        when_(arguments.side) {
            case_(CraftSideType.CLIENT.getName()) {
                val clientPid by stringVar()
                val isClientRunning by booleanVar()
                val isClientStopping by booleanVar()
                onInterrupt {
                    ifBlock {
                        if_(bash.conditions.isPidAlive(clientPid)) {
                            ifBlock {
                                if_(isClientRunning.equals_(true)) {
                                    clearLine()
                                    print_("Stopping ", AnsiColor.GRAY)
                                    printSide(ModSide.CLIENT, AnsiColor.GRAY)
                                    print_(" ${INTERRUPT_KEY.rounded()}...", AnsiColor.GRAY)
                                    setBooleanVarValue(isClientStopping, true)
                                }
                            }
                            kill(clientPid)
                            wait(clientPid)
                            ifBlock {
                                if_(isClientStopping.equals_(true)) {
                                    print_(" Done.", AnsiColor.GRAY)
                                    println_()
                                }
                            }
                        }
                    }
                    clearLine()
                    ifBlock {
                        if_(isClientRunning.equals_(false)) {
                            printError("Interrupted by user.")
                        }
                    }
                    return@onInterrupt this
                }
                val clientLogPath = runGradleTaskInBackground(
                    "craftClient",
                    unquotedLoader,
                    unquotedVersion,
                    modProjectName,
                    clientPid,
                )
                watchFileLines(clientLogPath, clientPid) { line ->
                    ifBlock {
                        if_(listOf(isClientRunning.equals_(false), isClientStopping.equals_(false))) {
                            ifBlock {
                                if_(
                                    listOf(
                                        line.contains("[Render thread/"),
                                        line.contains("[LWJGL]"),
                                        line.contains("fps,"),
                                    ),
                                    BashOperator.OR
                                ) {
                                    setBooleanVarValue(isClientRunning, true)
                                }
                            }
                        }
                    }
                    clearLine()
                    println_(line)
                    ifBlock {
                        if_(isClientRunning.equals_(false)) {
                            val currentSpinnerChar by stringVar(spinner.getCharAt(spinnerProgress.mod(spinnerLength)))
                            incrementIntVarValue(spinnerProgress)
                            print_(currentSpinnerChar, AnsiColor.CYAN)
                            print_(" Crafting ", AnsiColor.CYAN)
                            printSide(ModSide.CLIENT, AnsiColor.CYAN)
                            print_(" for ", AnsiColor.CYAN)
                            print_(displayedVersion, AnsiColor.CYAN, AnsiStyle.BOLD)
                            print_("...", AnsiColor.CYAN)
                        }.if_(isClientStopping.equals_(false)) {
                            printSide(ModSide.CLIENT, AnsiColor.GREEN)
                            print_(" for ", AnsiColor.GREEN)
                            print_(displayedVersion, AnsiColor.GREEN, AnsiStyle.BOLD)
                            print_(" is running...", AnsiColor.GREEN)
                            print_(" (Press ", AnsiColor.GRAY)
                            print_(INTERRUPT_KEY.angled(), AnsiColor.GRAY, AnsiStyle.BOLD)
                            print_(" to stop)", AnsiColor.GRAY)
                        }
                    }
                }
                val clientStatus = wait(clientPid)
                clearLine()
                ifBlock {
                    if_(clientStatus.equals_(0)) {
                        printSide(ModSide.CLIENT, AnsiColor.GRAY)
                        print_(" closed.", AnsiColor.GRAY)
                        println_()
                    }.else_ {
                        printSide(ModSide.CLIENT, AnsiColor.RED)
                        print_(" crashed. ", AnsiColor.RED)
                        print_("See full log at ${bash.getAbsolutePath(clientLogPath.value)}", AnsiColor.GRAY)
                        println_()
                        throw_()
                    }
                }
            }.case_(CraftSideType.SERVER.getName()) {
                val serverPid by stringVar()
                val isServerRunning by booleanVar()
                val isServerStopping by booleanVar()
                onInterrupt {
                    ifBlock {
                        if_(bash.conditions.isPidAlive(serverPid)) {
                            ifBlock {
                                if_(isServerRunning.equals_(true)) {
                                    clearLine()
                                    print_("Stopping ", AnsiColor.GRAY)
                                    printSide(ModSide.SERVER, AnsiColor.GRAY)
                                    print_(" ${INTERRUPT_KEY.rounded()}...", AnsiColor.GRAY)
                                    setBooleanVarValue(isServerStopping, true)
                                }
                            }
                            kill(serverPid)
                            wait(serverPid)
                            ifBlock {
                                if_(isServerStopping.equals_(true)) {
                                    print_(" Done.", AnsiColor.GRAY)
                                    println_()
                                }
                            }
                        }
                    }
                    clearLine()
                    ifBlock {
                        if_(isServerRunning.equals_(false)) {
                            printError("Interrupted by user.")
                        }
                    }
                    return@onInterrupt this
                }
                val serverLogPath = runGradleTaskInBackground(
                    "craftServer",
                    unquotedLoader,
                    unquotedVersion,
                    modProjectName,
                    serverPid,
                )
                watchFileLines(serverLogPath, serverPid) { line ->
                    ifBlock {
                        if_(listOf(isServerRunning.equals_(false), isServerStopping.equals_(false))) {
                            ifBlock {
                                if_(line.contains("Done")) {
                                    setBooleanVarValue(isServerRunning, true)
                                }
                            }
                        }
                    }
                    clearLine()
                    println_(line)
                    ifBlock {
                        if_(isServerRunning.equals_(false)) {
                            val currentSpinnerChar by stringVar(spinner.getCharAt(spinnerProgress.mod(spinnerLength)))
                            incrementIntVarValue(spinnerProgress)
                            print_(currentSpinnerChar, AnsiColor.CYAN)
                            print_(" Crafting ", AnsiColor.CYAN)
                            printSide(ModSide.SERVER, AnsiColor.CYAN)
                            print_(" for ", AnsiColor.CYAN)
                            print_(displayedVersion, AnsiColor.CYAN, AnsiStyle.BOLD)
                            print_("...", AnsiColor.CYAN)
                        }.if_(isServerStopping.equals_(false)) {
                            printSide(ModSide.SERVER, AnsiColor.GREEN)
                            print_(" for ", AnsiColor.GREEN)
                            print_(displayedVersion, AnsiColor.GREEN, AnsiStyle.BOLD)
                            print_(" is running...", AnsiColor.GREEN)
                            print_(" (Press ", AnsiColor.GRAY)
                            print_(INTERRUPT_KEY.angled(), AnsiColor.GRAY, AnsiStyle.BOLD)
                            print_(" to stop)", AnsiColor.GRAY)
                        }
                    }
                }
                val serverStatus = wait(serverPid)
                clearLine()
                ifBlock {
                    if_(serverStatus.equals_(0)) {
                        printSide(ModSide.SERVER, AnsiColor.GRAY)
                        print_(" stopped.", AnsiColor.GRAY)
                        println_()
                    }.else_ {
                        printSide(ModSide.SERVER, AnsiColor.RED)
                        print_(" crashed. ", AnsiColor.RED)
                        print_("See full log at ${bash.getAbsolutePath(serverLogPath.value)}", AnsiColor.GRAY)
                        println_()
                        throw_()
                    }
                }
            }.case_(CraftSideType.BOTH.getName()) {
                val serverPid by stringVar()
                val isServerRunning by booleanVar()
                val isServerStopping by booleanVar()
                val clientPid by stringVar()
                val isClientRunning by booleanVar()
                val isClientStopping by booleanVar()
                onInterrupt {
                    ifBlock {
                        if_(bash.conditions.isPidAlive(clientPid)) {
                            ifBlock {
                                if_(isClientRunning.equals_(true)) {
                                    clearLine()
                                    print_("Stopping ", AnsiColor.GRAY)
                                    printSide(ModSide.CLIENT, AnsiColor.GRAY)
                                    print_(" ${INTERRUPT_KEY.rounded()}...", AnsiColor.GRAY)
                                    setBooleanVarValue(isClientStopping, true)
                                }
                            }
                            kill(clientPid)
                            wait(clientPid)
                            ifBlock {
                                if_(isClientStopping.equals_(true)) {
                                    print_(" Done.", AnsiColor.GRAY)
                                }
                            }
                        }
                    }
                    ifBlock {
                        if_(bash.conditions.isPidAlive(serverPid)) {
                            ifBlock {
                                if_(isServerRunning.equals_(true)) {
                                    moveCursorUp()
                                    clearLine()
                                    print_("Stopping ", AnsiColor.GRAY)
                                    printSide(ModSide.SERVER, AnsiColor.GRAY)
                                    print_(" ${INTERRUPT_KEY.rounded()}...", AnsiColor.GRAY)
                                    setBooleanVarValue(isServerStopping, true)
                                }
                            }
                            kill(serverPid)
                            wait(serverPid)
                            ifBlock {
                                if_(isServerStopping.equals_(true)) {
                                    print_(" Done.", AnsiColor.GRAY)
                                    moveCursorDown()
                                    println_()
                                }
                            }
                        }
                    }
                    ifBlock {
                        if_(isServerRunning.equals_(false)) {
                            clearLine()
                            print_("Crafting ", AnsiColor.RED)
                            printSide(ModSide.SERVER, AnsiColor.RED)
                            print_(" for ", AnsiColor.RED)
                            print_(displayedVersion, AnsiColor.RED, AnsiStyle.BOLD)
                            print_(" was interrupted by user.", AnsiColor.RED)
                            println_()
                        }
                        if_(isClientRunning.equals_(false)) {
                            clearLine()
                            print_("Crafting ", AnsiColor.RED)
                            printSide(ModSide.CLIENT, AnsiColor.RED)
                            print_(" for ", AnsiColor.RED)
                            print_(displayedVersion, AnsiColor.RED, AnsiStyle.BOLD)
                            print_(" was interrupted by user.", AnsiColor.RED)
                            println_()
                        }
                    }
                    return@onInterrupt this
                }
                val serverLogPath = runGradleTaskInBackground(
                    "craftServer",
                    unquotedLoader,
                    unquotedVersion,
                    modProjectName,
                    serverPid,
                )
                watchFileLines(serverLogPath, serverPid) { line ->
                    ifBlock {
                        if_(line.contains("Done")) {
                            setBooleanVarValue(isServerRunning, true)
                            break_()
                        }
                    }
                    clearLine()
                    println_(line)
                    val currentSpinnerChar by stringVar(spinner.getCharAt(spinnerProgress.mod(spinnerLength)))
                    incrementIntVarValue(spinnerProgress)
                    print_(currentSpinnerChar, AnsiColor.CYAN)
                    print_(" Crafting ", AnsiColor.CYAN)
                    printSide(ModSide.SERVER, AnsiColor.CYAN)
                    print_(" for ", AnsiColor.CYAN)
                    print_(displayedVersion, AnsiColor.CYAN, AnsiStyle.BOLD)
                    print_("...", AnsiColor.CYAN)
                }
                val clientLogPath = runGradleTaskInBackground(
                    "craftClient",
                    unquotedLoader,
                    unquotedVersion,
                    modProjectName,
                    clientPid,
                )
                watchFileLines(clientLogPath, clientPid) { line ->
                    ifBlock {
                        if_(listOf(isClientRunning.equals_(false), isClientStopping.equals_(false))) {
                            ifBlock {
                                if_(
                                    listOf(
                                        line.contains("[Render thread/"),
                                        line.contains("[LWJGL]"),
                                        line.contains("fps,"),
                                    ),
                                    BashOperator.OR
                                ) {
                                    setBooleanVarValue(isClientRunning, true)
                                }
                            }
                        }
                    }
                    clearLine()
                    moveCursorUp()
                    clearLine()
                    println_(line)
                    printSide(ModSide.SERVER, AnsiColor.GREEN)
                    print_(" for ", AnsiColor.GREEN)
                    print_(displayedVersion, AnsiColor.GREEN, AnsiStyle.BOLD)
                    print_(" is running...", AnsiColor.GREEN)
                    print_(" (Press ", AnsiColor.GRAY)
                    print_(INTERRUPT_KEY.angled(), AnsiColor.GRAY, AnsiStyle.BOLD)
                    print_(" to stop)", AnsiColor.GRAY)
                    println_()
                    ifBlock {
                        if_(isClientRunning.equals_(false)) {
                            val currentSpinnerChar by stringVar(spinner.getCharAt(spinnerProgress.mod(spinnerLength)))
                            incrementIntVarValue(spinnerProgress)
                            print_(currentSpinnerChar, AnsiColor.CYAN)
                            print_(" Crafting ", AnsiColor.CYAN)
                            printSide(ModSide.CLIENT, AnsiColor.CYAN)
                            print_(" for ", AnsiColor.CYAN)
                            print_(displayedVersion, AnsiColor.CYAN, AnsiStyle.BOLD)
                            print_("...", AnsiColor.CYAN)
                        }.if_(isClientStopping.equals_(false)) {
                            printSide(ModSide.CLIENT, AnsiColor.GREEN)
                            print_(" for ", AnsiColor.GREEN)
                            print_(displayedVersion, AnsiColor.GREEN, AnsiStyle.BOLD)
                            print_(" is running...", AnsiColor.GREEN)
                            print_(" (Press ", AnsiColor.GRAY)
                            print_(INTERRUPT_KEY.angled(), AnsiColor.GRAY, AnsiStyle.BOLD)
                            print_(" to stop)", AnsiColor.GRAY)
                        }
                    }
                }
                val clientStatus = wait(clientPid)
                clearLine()
                ifBlock {
                    if_(clientStatus.equals_(0)) {
                        printSide(ModSide.CLIENT, AnsiColor.GRAY)
                        print_(" closed.", AnsiColor.GRAY)
                    }.else_ {
                        printSide(ModSide.CLIENT, AnsiColor.RED)
                        print_(" crashed. ", AnsiColor.RED)
                        print_("See full log at ${bash.getAbsolutePath(clientLogPath.value)}", AnsiColor.GRAY)
                    }
                }
                val serverStatus = wait(serverPid)
                moveCursorUp()
                clearLine()
                ifBlock {
                    if_(serverStatus.equals_(0)) {
                        printSide(ModSide.SERVER, AnsiColor.GRAY)
                        print_(" stopped.", AnsiColor.GRAY)
                        println_()
                    }.else_ {
                        printSide(ModSide.SERVER, AnsiColor.RED)
                        print_(" crashed. ", AnsiColor.RED)
                        print_("See full log at ${bash.getAbsolutePath(serverLogPath.value)}", AnsiColor.GRAY)
                        println_()
                        throw_()
                    }
                }
            }
        }
        return@bashScript this
    }

    private fun BashScriptBuilder.printSide(side: ModSide, color: AnsiColor): BashScriptBuilder {
        print_(MinecraftConstants.FULL_GAME_NAME, color)
        print_(" ${side.getName()}", color, AnsiStyle.BOLD)
        return this
    }
}
