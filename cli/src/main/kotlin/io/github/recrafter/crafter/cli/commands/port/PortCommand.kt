package io.github.recrafter.crafter.cli.commands.port

import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.versions.MinecraftVersionRange.Companion.MOD_PROJECT_NAME_SEPARATOR
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.bash.ExitCode
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor.*
import io.github.recrafter.crafter.cli.bash.ansi.AnsiStyle.BOLD
import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.bash.api.commands.AbstractCLICommand
import io.github.recrafter.crafter.cli.bash.conditions.BashConditions.isDirectoryExists
import io.github.recrafter.crafter.cli.bash.conditions.not_
import io.github.recrafter.crafter.cli.bash.properties.*
import io.github.recrafter.crafter.cli.bash.utils.Cmd
import io.github.recrafter.crafter.cli.bash.variables.*
import io.github.recrafter.crafter.cli.commands.craft.CraftCommand
import io.github.recrafter.crafter.cli.commands.process.Spinner
import io.github.recrafter.crafter.cli.extensions.common.script
import io.github.recrafter.crafter.cli.extensions.common.spaced
import io.github.recrafter.crafter.cli.extensions.common.withScript

@CLICommand(name = PortCommand.COMMAND_NAME, description = "Port the mod to older or newer Minecraft versions")
object PortCommand : AbstractCLICommand<PortArguments>(PortArguments.serializer()) {

    private const val COMMAND_NAME: String = "port"
    private const val PORT_RANGE_BOUNDARY: String = COMMAND_NAME

    override fun getCompletions(argumentName: String, arguments: PortArguments): String = withScript {
        when (argumentName) {
            arguments::loader.name -> bash.getString("LOADERS").value
            else -> failWithInvalidValue(argumentName)
        }
    }

    override fun run(fingerprint: Fingerprint, arguments: PortArguments): String = script {
        val loaderCheckpoints by mapVar(fingerprint.loaders.associate {
            it.name to it.checkpoints.joinBySpace { version -> version.asString() }
        })
        val checkpoints by arrayVar(loaderCheckpoints.getValue(arguments.loader))
        val loaderDisplayName by stringVar(bash.getMap("LOADER_DISPLAY_NAMES").getValue(arguments.loader))
        val modProjectPaths by arrayVar(arguments.loader.value.appendPath("*"))
        ifBlock {
            if_(modProjectPaths.isEmpty()) {
                print_("No projects found for the ", RED)
                print_(loaderDisplayName, RED, BOLD)
                print_(" loader.", RED)
                println_()
                error_()
            }
        }
        val portProjectPaths by arrayVar()
        forEach_(modProjectPaths) { modProjectPath ->
            ifBlock {
                if_(bash.conditions.isDirectoryExists(modProjectPath).not_()) {
                    continue_()
                }
            }
            val directoryName by stringVar(bash.getBasename(modProjectPath).command)
            ifBlock {
                if_(arguments.direction.equals_(PortDirection.PAST)) {
                    ifBlock {
                        if_(directoryName.startsWith(PORT_RANGE_BOUNDARY + MOD_PROJECT_NAME_SEPARATOR)) {
                            addToArray(portProjectPaths, modProjectPath)
                        }
                    }
                }.else_ {
                    ifBlock {
                        if_(directoryName.endsWith(MOD_PROJECT_NAME_SEPARATOR + PORT_RANGE_BOUNDARY)) {
                            addToArray(portProjectPaths, modProjectPath)
                        }
                    }
                }
            }
        }
        ifBlock {
            if_(portProjectPaths.size.isGreaterThen(1)) {
                print_("Multiple active port sessions detected:", RED)
                println_()
                println_()
                forEach_(portProjectPaths) { path ->
                    withPadding {
                        println_(path, RED, BOLD)
                    }
                }
                println_()
                println_("Only one port session may be active at a time.", RED)
                error_()
            }
        }
        val currentPortPath by stringVar(portProjectPaths.getElement(0))
        val currentPortVersion by stringVar()
        ifBlock {
            if_(arguments.step.equals_(PortStep.START)) {
                ifBlock {
                    if_(currentPortPath.isNotEmpty()) {
                        print_("Found an existing port session at ", RED)
                        print_(portProjectPaths.value, RED, BOLD)
                        println_()
                        print_("You must continue, stop or cancel the current port session first.", RED)
                        println_()
                        error_()
                    }
                }
            }.else_ {
                ifBlock {
                    if_(portProjectPaths.isEmpty()) {
                        println_("No active port session found. Start one with:", RED)
                        println_()
                        withPadding {
                            println_(buildStepCommand(fingerprint, arguments, PortStep.START), CYAN)
                        }
                        println_()
                        error_()
                    }
                }
                ifBlock {
                    if_(arguments.step.equals_(PortStep.CANCEL)) {
                        deleteDirectory(currentPortPath, recursive = true)
                        exit(ExitCode.SUCCESS)
                    }
                }
                val currentPortProjectName by stringVar(bash.getBasename(currentPortPath).command)
                ifBlock {
                    if_(arguments.direction.equals_(PortDirection.PAST)) {
                        setStringValue(
                            currentPortVersion,
                            currentPortProjectName.removePrefix(PORT_RANGE_BOUNDARY + MOD_PROJECT_NAME_SEPARATOR)
                        )
                    }.else_ {
                        setStringValue(
                            currentPortVersion,
                            currentPortProjectName.removeSuffix(MOD_PROJECT_NAME_SEPARATOR + PORT_RANGE_BOUNDARY)
                        )
                    }
                }
                ifBlock {
                    if_(arguments.step.equals_(PortStep.STOP)) {
                        renameDirectory(currentPortPath, arguments.loader.value.appendPath(currentPortVersion.value))
                        exit(ExitCode.SUCCESS)
                    }
                }
            }
        }
        val versionsTimeline by arrayVar(bash.getMap("VERSIONS").getValue(arguments.loader))
        ifBlock {
            if_(arguments.direction.equals_(PortDirection.FUTURE)) {
                setArrayValue(versionsTimeline, reverseArray(versionsTimeline))
            }
        }
        ifBlock {
            if_(arguments.step.equals_(PortStep.CONTINUE)) {
                val lastSourceDirectoryPath by stringVar()
                val lastSourceVersion by stringVar()
                val oppositeVersion by stringVar()
                forEach_(versionsTimeline) { version ->
                    val isFound by booleanVar()
                    forEach_(modProjectPaths) { modProjectPath ->
                        val directoryName by stringVar(bash.getBasename(modProjectPath).command)
                        val rangeParts by arrayVar(directoryName.split(MOD_PROJECT_NAME_SEPARATOR))
                        val rangeMin by stringVar(rangeParts.getElement(0))
                        val rangeMax by stringVar(rangeParts.getElement(1))
                        ifBlock {
                            if_(arguments.direction.equals_(PortDirection.PAST)) {
                                ifBlock {
                                    ifAny(
                                        directoryName.equals_(version),
                                        directoryName.startsWith(version.value + MOD_PROJECT_NAME_SEPARATOR)
                                    ) {
                                        setBooleanValue(isFound, true)
                                        setStringValue(lastSourceDirectoryPath, modProjectPath)
                                        ifBlock {
                                            ifAll(rangeMin.isNotEmpty(), rangeMax.isNotEmpty()) {
                                                setStringValue(lastSourceVersion, rangeMin)
                                                setStringValue(oppositeVersion, rangeMax)
                                            }.else_ {
                                                setStringValue(lastSourceVersion, directoryName)
                                                setStringValue(oppositeVersion, directoryName)
                                            }
                                        }
                                        break_()
                                    }
                                }
                            }.else_ {
                                ifBlock {
                                    ifAny(
                                        directoryName.equals_(version),
                                        directoryName.endsWith(MOD_PROJECT_NAME_SEPARATOR + version.value)
                                    ) {
                                        setBooleanValue(isFound, true)
                                        setStringValue(lastSourceDirectoryPath, modProjectPath)
                                        ifBlock {
                                            ifAll(rangeMin.isNotEmpty(), rangeMax.isNotEmpty()) {
                                                setStringValue(lastSourceVersion, rangeMax)
                                                setStringValue(oppositeVersion, rangeMin)
                                            }.else_ {
                                                setStringValue(lastSourceVersion, directoryName)
                                                setStringValue(oppositeVersion, directoryName)
                                            }
                                        }
                                        break_()
                                    }
                                }
                            }
                        }
                    }
                    ifBlock {
                        if_(isFound.equals_(true)) {
                            break_()
                        }
                    }
                }
                val isCheckpoint by booleanVar()
                ifBlock {
                    if_(arguments.direction.equals_(PortDirection.PAST)) {
                        ifBlock {
                            if_(checkpoints.contains(lastSourceVersion)) {
                                setBooleanValue(isCheckpoint, true)
                            }
                        }
                    }.else_ {
                        ifBlock {
                            if_(checkpoints.contains(currentPortVersion)) {
                                setBooleanValue(isCheckpoint, true)
                            }
                        }
                    }
                }
                ifBlock {
                    if_(isCheckpoint.equals_(true)) {
                        renameDirectory(currentPortPath, arguments.loader.value.appendPath(currentPortVersion.value))
                    }.else_ {
                        ifBlock {
                            if_(arguments.direction.equals_(PortDirection.PAST)) {
                                renameDirectory(
                                    currentPortPath,
                                    arguments.loader.value.appendPath(
                                        currentPortVersion.value + MOD_PROJECT_NAME_SEPARATOR + oppositeVersion.value
                                    )
                                )
                            }.else_ {
                                renameDirectory(
                                    currentPortPath,
                                    arguments.loader.value.appendPath(
                                        oppositeVersion.value + MOD_PROJECT_NAME_SEPARATOR + currentPortVersion.value
                                    )
                                )
                            }
                        }
                        deleteDirectory(lastSourceDirectoryPath, recursive = true)
                    }
                }
                setArrayValue(modProjectPaths, arguments.loader.value.appendPath("*"))
            }
        }
        val sourceDirectoryPath by stringVar()
        val targetVersionIndex by intVar()
        forEachIndexed(versionsTimeline) { index, version ->
            val isFound by booleanVar()
            forEach_(modProjectPaths) { modProjectPath ->
                val directoryName by stringVar(bash.getBasename(modProjectPath).command)
                ifBlock {
                    if_(arguments.direction.equals_(PortDirection.PAST)) {
                        ifBlock {
                            ifAny(
                                directoryName.equals_(version),
                                directoryName.startsWith(version.value + MOD_PROJECT_NAME_SEPARATOR)
                            ) {
                                setBooleanValue(isFound, true)
                                setStringValue(sourceDirectoryPath, modProjectPath)
                                setIntValue(targetVersionIndex, index.minus(1))
                                break_()
                            }
                        }
                    }.else_ {
                        ifBlock {
                            ifAny(
                                directoryName.equals_(version),
                                directoryName.endsWith(MOD_PROJECT_NAME_SEPARATOR + version.value)
                            ) {
                                setBooleanValue(isFound, true)
                                setStringValue(sourceDirectoryPath, modProjectPath)
                                setIntValue(targetVersionIndex, index.minus(1))
                                break_()
                            }
                        }
                    }
                }
            }
            ifBlock {
                if_(isFound.equals_(true)) {
                    break_()
                }
            }
        }
        ifBlock {
            if_(targetVersionIndex.isLessThen(0)) {
                print_("The mod already supports the ", YELLOW)
                ifBlock {
                    if_(arguments.direction.equals_(PortDirection.PAST)) {
                        print_("oldest", YELLOW)
                    }.else_ {
                        print_("newest", YELLOW)
                    }
                }
                print_(" Minecraft version available for ", YELLOW)
                print_(loaderDisplayName, YELLOW, BOLD)
                print_(" loader: ", YELLOW)
                print_(versionsTimeline.getElement(0), YELLOW, BOLD)
                println_()
                error_()
            }
        }
        val targetVersion by stringVar(versionsTimeline.getElement(targetVersionIndex))
        val modProjectName by stringVar()
        ifBlock {
            if_(arguments.step.equals_(PortStep.TEST)) {
                setStringValue(modProjectName, bash.getBasename(currentPortPath).command)
            }.if_(arguments.direction.equals_(PortDirection.PAST)) {
                setStringValue(modProjectName, PORT_RANGE_BOUNDARY + MOD_PROJECT_NAME_SEPARATOR + targetVersion.value)
            }.else_ {
                setStringValue(modProjectName, targetVersion.value + MOD_PROJECT_NAME_SEPARATOR + PORT_RANGE_BOUNDARY)
            }
        }
        ifBlock {
            if_(arguments.step.equals_(PortStep.TEST).not_()) {
                val targetDirectoryPath by stringVar(arguments.loader.value.appendPath(modProjectName.value))
                ifBlock {
                    val sourceRootSrc = sourceDirectoryPath.value.appendPath("src")
                    val targetRootSrc = targetDirectoryPath.value.appendPath("src")
                    if_(bash.conditions.isDirectoryExists(sourceRootSrc)) {
                        copyDirectory(sourceRootSrc, targetRootSrc)
                    }
                }
                fingerprint.modSides.forEach { side ->
                    val sourceSideRootSrc = sourceDirectoryPath.value.appendPath(side.getName()).appendPath("src")
                    val targetSideRootSrc = targetDirectoryPath.value.appendPath(side.getName()).appendPath("src")
                    ifBlock {
                        if_(bash.conditions.isDirectoryExists(sourceSideRootSrc)) {
                            copyDirectory(sourceSideRootSrc, targetSideRootSrc)
                        }
                    }
                }
                return@if_ this
            }
        }
        val spinner = Spinner.build(this)
        ifBlock {
            if_(arguments.strategy.equals_(PortStrategy.PENDING)) {
                cursor.hide()
                print_("Please test whether the mod works correctly on", BLUE)
                print_(" $loaderDisplayName $targetVersion", BLUE, BOLD)
                print_(" using the test launcher below.", BLUE)
                println_()
                CraftCommand.craftLauncher(this, arguments.loader, targetVersion, modProjectName, spinner) {
                    println_()
                    print_("Test completed for", BLUE)
                    print_(" $loaderDisplayName $targetVersion", BLUE, BOLD)
                    println_()
                    println_()
                    print_("To")
                    print_(" test again", BLUE, BOLD)
                    print_(", run:")
                    println_()
                    println_()
                    withPadding {
                        println_(buildStepCommand(fingerprint, arguments, PortStep.TEST), CYAN)
                    }
                    println_()
                    print_("If the mod")
                    print_(" works correctly", GREEN, BOLD)
                    print_(", continue the port:")
                    println_()
                    println_()
                    withPadding {
                        println_(buildStepCommand(fingerprint, arguments, PortStep.CONTINUE), CYAN)
                    }
                    println_()
                    print_("If the mod")
                    print_(" doesn't work ", RED, BOLD)
                    print_("or crashes, stop the port:")
                    println_()
                    println_()
                    withPadding {
                        println_(buildStepCommand(fingerprint, arguments, PortStep.STOP), CYAN)
                    }
                    println_()
                    print_("If you")
                    print_(" don't want to support ", GRAY, BOLD)
                    print_("this version, cancel the port:")
                    println_()
                    println_()
                    withPadding {
                        println_(buildStepCommand(fingerprint, arguments, PortStep.CANCEL), CYAN)
                    }
                    println_()
                }
            }.else_ {
                println_("The ${PortStrategy.AUTO.getName()} strategy is under development!", YELLOW)
                println_(
                    "In version 2.0, it will build the mod and continue or stop depending on the build result.",
                    YELLOW
                )
            }
        }
    }

    private fun buildStepCommand(fingerprint: Fingerprint, arguments: PortArguments, step: PortStep): String =
        Cmd.of(
            fingerprint.scriptName,
            spaced(COMMAND_NAME, arguments.loader, step.getName(), arguments.direction, arguments.strategy)
        )
}
