package io.github.recrafter.crafter.cli.commands.init

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.`dot․case`
import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.diskria.kotlin.utils.extensions.common.`path∕case`
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.ascii.Spinners
import io.github.recrafter.crafter.cli.bash.builder.*
import io.github.recrafter.crafter.cli.bash.builder.Conditions.isDirectoryExists
import io.github.recrafter.crafter.cli.bash.builder.Conditions.isPidAlive
import io.github.recrafter.crafter.cli.commands.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.commands.api.common.AbstractCLICommand
import io.github.recrafter.crafter.cli.extensions.common.bashScript
import io.github.recrafter.crafter.cli.extensions.common.withBashScript
import io.github.recrafter.crafter.cli.extensions.unquoted
import io.github.recrafter.crafter.cli.properties.intVar
import io.github.recrafter.crafter.cli.properties.stringVar
import io.github.recrafter.crafter.core.helpers.MixinsHelper
import org.gradle.api.tasks.SourceSet

@CLICommand(name = "init", description = "Create and initialize new mod project")
object InitCommand : AbstractCLICommand<InitArguments>(InitArguments.serializer()) {

    override fun getCompletions(argumentName: String, arguments: InitArguments): String = withBashScript {
        when (argumentName) {
            arguments::loader.name -> bash.getStringVar("LOADERS").value
            arguments::version.name -> bash.getMapVar("VERSIONS").getValue(arguments.loader)
            else -> failWithInvalidValue(argumentName)
        }
    }

    override fun run(fingerprint: Fingerprint, arguments: InitArguments): String = bashScript {
        val unquotedLoader = arguments.loader.unquoted()
        val unquotedVersion = arguments.version.unquoted()
        val modProjectPath = unquotedLoader.appendPath(unquotedVersion)
        val versionToDisplay = buildString {
            append(bash.getMapVar("LOADER_DISPLAY_NAMES").getValue(arguments.loader))
            append(Constants.Char.SPACE)
            append(arguments.version)
        }
        ifBlock {
            if_(bash.conditions.isDirectoryExists(modProjectPath)) {
                print_("The mod for ", AnsiColor.RED)
                print_(versionToDisplay, AnsiColor.RED, AnsiStyle.BOLD)
                print_(" already initialized.", AnsiColor.RED)
                println_()
                throw_()
            }
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
            deleteRecursively(modProjectPath)
            clearLine()
            println_("Interrupted by user.", AnsiColor.RED)
            return@onInterrupt this
        }
        val spinner by stringVar(Spinners.DOTS)
        val spinnerProgress by intVar()
        val spinnerLength by intVar(spinner.length)
        val logPath = runGradleTaskInBackground("build", unquotedLoader, unquotedVersion, spinner, pid)
        watchFileLines(logPath, pid) { line ->
            clearLine()
            println_(line)
            val currentSpinnerChar by stringVar(spinner.getCharAt(spinnerProgress.mod(spinnerLength)))
            incrementIntVarValue(spinnerProgress)
            print_(currentSpinnerChar, AnsiColor.CYAN)
            print_(" Initializing mod for ", AnsiColor.CYAN)
            print_(versionToDisplay, AnsiColor.CYAN, AnsiStyle.BOLD)
            print_("...", AnsiColor.CYAN)
        }
        val status = wait(pid)
        clearLine()
        ifBlock {
            if_(status.equals_(0)) {
                print_("Successfully initialized mod for ", AnsiColor.GREEN)
                print_(versionToDisplay, AnsiColor.GREEN, AnsiStyle.BOLD)
                println_()
            }.else_ {
                print_("Failed to initialize mod for ", AnsiColor.RED)
                print_(versionToDisplay, AnsiColor.RED, AnsiStyle.BOLD)
                println_()
                println_("See full log at ${bash.getAbsolutePath(logPath.value)}", AnsiColor.GRAY)
                throw_()
            }
        }
    }
}
