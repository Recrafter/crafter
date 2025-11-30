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
import io.github.recrafter.crafter.cli.properties.arrayVar
import io.github.recrafter.crafter.cli.properties.intVar
import io.github.recrafter.crafter.cli.properties.stringVar

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
        val loaderDisplayName = bash.getMapVar("LOADER_DISPLAY_NAMES").getValue(arguments.loader)
        ifBlock {
            if_(bash.conditions.isDirectoryExists(modProjectPath)) {
                print_("The mod for ", color = AnsiColor.RED)
                print_("$loaderDisplayName ${arguments.version}", color = AnsiColor.RED, style = AnsiStyle.BOLD)
                print_(" already initialized.", color = AnsiColor.RED)
                println_()
                throw_()
            }
        }
        fingerprint.modSides.forEach { side ->
            val sideName = side.getName()
            val mixinsPackagePath = modProjectPath
                .appendPath(sideName)
                .appendPath("src/mixins/java")
                .appendPath(fingerprint.modNamespace.setCase(`dot․case`, `path∕case`))
                .appendPath(fingerprint.modId)
                .appendPath("mixins")
                .appendPath(sideName)
            createDirectory(mixinsPackagePath, recursive = true)
        }
        val pid by stringVar()
        onInterrupt {
            ifBlock {
                if_(bash.conditions.isPidAlive(pid)) {
                    run_("kill", pid)
                    run_("wait", pid)
                }
            }
            deleteRecursively(modProjectPath)
            clearLastLine()
            println_("Interrupted by user.", color = AnsiColor.RED)
            return@onInterrupt this
        }
        val spinner by stringVar(Spinners.DOTS)
        val spinnerProgress by intVar(0)
        val spinnerLength by intVar(spinner.length)
        val logPath = runGradleTaskInBackground("build", unquotedLoader, unquotedVersion, pid)
        val history by arrayVar()
        watchFileLines(logPath, pid) { line ->
            addToArrayVar(history, line)
            ifBlock {
                if_(history.size.isGreaterThen(7)) {
                    setArrayVarValue(history, history.takeLast(7))
                }
            }
            val currentSpinnerChar by stringVar(spinner.getCharAt(spinnerProgress.mod(spinnerLength)))
            incrementIntVarValue(spinnerProgress)
            clearLastLine()
            println_(line)
            print_(currentSpinnerChar.toString(), color = AnsiColor.GREEN)
            print_(" ")
            print_("Initializing...")
        }
        run_("wait", pid)
        val status by intVar("$?")
        clearLastLine()
        ifBlock {
            val displayedVersion = loaderDisplayName + Constants.Char.SPACE + arguments.version
            if_(status.equals_(0)) {
                println_("Successfully initialized mod for $displayedVersion", color = AnsiColor.GREEN)
            }.else_ {
                println_("Failed to initialize mod for $displayedVersion", color = AnsiColor.RED)
                println_("See full log at ${logPath.value}", color = AnsiColor.GRAY)
                throw_()
            }
        }
    }
}
