package io.github.recrafter.crafter.cli.commands.init

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.`dot․case`
import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.diskria.kotlin.utils.extensions.common.`path∕case`
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.bash.ExitCode
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor
import io.github.recrafter.crafter.cli.bash.ansi.AnsiStyle
import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.bash.api.commands.AbstractCLICommand
import io.github.recrafter.crafter.cli.bash.ascii.Spinners
import io.github.recrafter.crafter.cli.bash.conditions.BashConditions.isPidAlive
import io.github.recrafter.crafter.cli.bash.properties.intVar
import io.github.recrafter.crafter.cli.bash.properties.stringVar
import io.github.recrafter.crafter.cli.bash.variables.*
import io.github.recrafter.crafter.cli.extensions.common.script
import io.github.recrafter.crafter.cli.extensions.common.withScript
import io.github.recrafter.crafter.core.helpers.MixinsHelper
import org.gradle.api.tasks.SourceSet

@CLICommand(name = "init", description = "Create and initialize new mod project")
object InitCommand : AbstractCLICommand<InitArguments>(InitArguments.serializer()) {

    override fun getCompletions(argumentName: String, arguments: InitArguments): String = withScript {
        when (argumentName) {
            arguments::loader.name -> bash.getString("LOADERS").value
            arguments::version.name -> bash.getMap("VERSIONS").getValue(arguments.loader)
            else -> failWithInvalidValue(argumentName)
        }
    }

    override fun run(fingerprint: Fingerprint, arguments: InitArguments): String = script {
        val modProjectPath = arguments.loader.value.appendPath(arguments.version.value)
        val displayedVersion = buildString {
            append(bash.getMap("LOADER_DISPLAY_NAMES").getValue(arguments.loader))
            append(Constants.Char.SPACE)
            append(arguments.version)
        }
        findModProject(arguments.loader, arguments.version) { projectName, isRange ->
            if (isRange) {
                print_("The mod for ", AnsiColor.RED)
                print_(displayedVersion, AnsiColor.RED, AnsiStyle.BOLD)
                print_(" already included in version range ", AnsiColor.RED)
                print_(projectName, AnsiColor.RED, AnsiStyle.BOLD)
            } else {
                print_("The mod for ", AnsiColor.RED)
                print_(displayedVersion, AnsiColor.RED, AnsiStyle.BOLD)
                print_(" already initialized.", AnsiColor.RED)
            }
            println_()
            throw_()
        }
        val namespacePath = fingerprint.modNamespace.setCase(`dot․case`, `path∕case`).appendPath(fingerprint.modId)
        fingerprint.modSides.forEach { side ->
            val modMain = modProjectPath.appendPath("src").appendPath(SourceSet.MAIN_SOURCE_SET_NAME).appendPath("java")
            createDirectory(modMain.appendPath(namespacePath))

            val sideName = side.getName()
            val sideSources = modProjectPath.appendPath(sideName).appendPath("src")
            val sideMain = sideSources.appendPath(SourceSet.MAIN_SOURCE_SET_NAME)
            createDirectory(sideMain.appendPath("resources"))
            createDirectory(sideMain.appendPath("java").appendPath(namespacePath).appendPath(sideName))

            val sideMixins = sideSources.appendPath(MixinsHelper.MIXINS_NAME)
            createDirectory(
                sideMixins.appendPath("java")
                    .appendPath(namespacePath)
                    .appendPath(MixinsHelper.MIXINS_NAME)
                    .appendPath(sideName)
            )
        }
        val pid by stringVar()
        onInterrupt {
            ifBlock {
                if_(bash.conditions.isPidAlive(pid)) {
                    kill(pid)
                    wait(pid)
                    return@if_ this
                }
            }
            delete(modProjectPath, recursive = true)
            clearLine()
            print_("Initializing mod for ", AnsiColor.RED)
            print_(displayedVersion, AnsiColor.RED, AnsiStyle.BOLD)
            print_(" was interrupted by user.", AnsiColor.RED)
            println_()
        }
        val spinner by stringVar(Spinners.DOTS)
        val spinnerProgress by intVar()
        val spinnerLength by intVar(spinner.length)
        val logPath by stringVar()
        runGradleTask("build", arguments.loader, arguments.version, arguments.version, pid, logPath)
        watchFileLines(logPath, pid, background = true) { line ->
            clearLine()
            println_(line)
        }
        while_(bash.conditions.isPidAlive(pid)) {
            val spinnerChar by stringVar(spinner.getCharAt(spinnerProgress.mod(spinnerLength)))
            incrementIntValue(spinnerProgress)
            clearLine()
            print_(spinnerChar, AnsiColor.CYAN)
            print_(" Initializing mod for ", AnsiColor.CYAN)
            print_(displayedVersion, AnsiColor.CYAN, AnsiStyle.BOLD)
            print_("...", AnsiColor.CYAN)
            sleep(0.03f)
        }
        val exitCode = wait(pid)
        clearLine()
        ifBlock {
            if_(exitCode.equals_(ExitCode.SUCCESS)) {
                print_("Successfully initialized mod for ", AnsiColor.GREEN)
                print_(displayedVersion, AnsiColor.GREEN, AnsiStyle.BOLD)
                println_()
            }.else_ {
                print_("Initialization mod for ", AnsiColor.RED)
                print_(displayedVersion, AnsiColor.RED, AnsiStyle.BOLD)
                print_(" has been failed. ", AnsiColor.RED)
                print_("See full log at ${bash.getAbsolutePath(logPath.value)}", AnsiColor.GRAY)
                println_()
                throw_()
            }
        }
    }
}
