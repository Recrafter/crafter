package io.github.recrafter.crafter.cli.commands.init

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.`dot․case`
import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.diskria.kotlin.utils.extensions.common.`path∕case`
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor
import io.github.recrafter.crafter.cli.bash.ansi.AnsiStyle
import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.bash.variables.getValue
import io.github.recrafter.crafter.cli.bash.variables.value
import io.github.recrafter.crafter.cli.commands.process.GradleProcessCommand
import io.github.recrafter.crafter.cli.commands.process.InitProcess
import io.github.recrafter.crafter.cli.commands.process.Spinner
import io.github.recrafter.crafter.cli.extensions.common.script
import io.github.recrafter.crafter.cli.extensions.common.withScript
import io.github.recrafter.crafter.core.helpers.MixinsHelper
import org.gradle.api.tasks.SourceSet

@CLICommand(name = InitCommand.COMMAND_NAME, description = "Create and initialize new mod project")
object InitCommand : GradleProcessCommand<InitArguments>(InitArguments.serializer()) {

    const val COMMAND_NAME: String = "init"

    override fun getCompletions(argumentName: String, arguments: InitArguments): String = withScript {
        when (argumentName) {
            arguments::loader.name -> bash.getString("LOADERS").value
            arguments::version.name -> bash.getMap("VERSIONS").getValue(arguments.loader)
            else -> failWithInvalidValue(argumentName)
        }
    }

    override fun run(fingerprint: Fingerprint, arguments: InitArguments): String = script {
        val loader = arguments.loader
        val version = arguments.version
        val modProjectPath = loader.value.appendPath(version.value)
        val displayedVersion = buildString {
            append(bash.getMap("LOADER_DISPLAY_NAMES").getValue(loader))
            append(Constants.Char.SPACE)
            append(version)
        }
        detectModProjectName(loader, version) { name, isRange ->
            if (isRange) {
                print_("The mod for ", AnsiColor.RED)
                print_(displayedVersion, AnsiColor.RED, AnsiStyle.BOLD)
                print_(" already included in version range ", AnsiColor.RED)
                print_(name, AnsiColor.RED, AnsiStyle.BOLD)
            } else {
                print_("The mod for ", AnsiColor.RED)
                print_(displayedVersion, AnsiColor.RED, AnsiStyle.BOLD)
                print_(" already initialized.", AnsiColor.RED)
            }
            println_()
            error_()
        }
        val namespacePath = fingerprint.modNamespace.setCase(`dot․case`, `path∕case`).appendPath(fingerprint.modId)
        fingerprint.modSides.forEach { side ->
            val modMain = modProjectPath.appendPath("src")
                .appendPath(SourceSet.MAIN_SOURCE_SET_NAME)
                .appendPath("java")
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
        val spinner = Spinner.build(this)
        cursor.hide()
        val process = InitProcess(this, modProjectPath, displayedVersion)
        onExit {
            cursor.show()
            ensureProcessKilled(process.pid)
            ensureProcessKilled(process.logWatcher.pid)
        }
        onInterrupt {
            process.interrupt(this)
        }
        runTask(loader, version, version, process)
        process.runLogWatcher(this)
        loop {
            onNextVar(process.logWatcher.queue) {
                clearLine()
                process.printLogLine(this)
                println_()
            }
            process.printStatus(this, spinner)
        }
    }
}
