package io.github.recrafter.crafter.cli.commands.process

import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor
import io.github.recrafter.crafter.cli.bash.ansi.AnsiStyle
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.properties.stringVar
import io.github.recrafter.crafter.cli.bash.variables.getCharAt
import io.github.recrafter.crafter.cli.bash.variables.mod
import io.github.recrafter.crafter.cli.bash.variables.value
import io.github.recrafter.crafter.cli.commands.init.InitCommand
import io.github.recrafter.crafter.cli.extensions.common.Builder

class InitProcess(
    builder: ScriptBuilder,
    modProjectPath: String,
    displayedVersion: String,
) : GradleProcess(builder, "build", InitCommand.COMMAND_NAME) {

    override val onPrepare: ScriptBuilder.(spinner: Spinner) -> ScriptBuilder = { spinner ->
        val spinnerChar by stringVar(spinner.chars.getCharAt(spinner.progress.mod(spinner.length)))
        print_(spinnerChar, AnsiColor.CYAN)
        print_(" Initializing mod for ", AnsiColor.CYAN)
        print_(displayedVersion, AnsiColor.CYAN, AnsiStyle.BOLD)
        print_("...", AnsiColor.CYAN)
        incrementIntValue(spinner.progress)
    }

    override val onSuccess: Builder<ScriptBuilder> = {
        print_("Successfully initialized mod for ", AnsiColor.GREEN)
        print_(displayedVersion, AnsiColor.GREEN, AnsiStyle.BOLD)
        println_()
        break_()
    }

    override val onError: Builder<ScriptBuilder> = {
        print_("Initialization mod for ", AnsiColor.RED)
        print_(displayedVersion, AnsiColor.RED, AnsiStyle.BOLD)
        print_(" has been failed. ", AnsiColor.RED)
        print_("See full log at ${bash.getAbsolutePath(logWatcher.path.value)}", AnsiColor.GRAY)
        println_()
        error_()
    }

    override val onCancel: Builder<ScriptBuilder> = {
        deleteDirectory(modProjectPath, recursive = true)
    }

    override val onInterrupted: Builder<ScriptBuilder> = {
        print_("Initializing mod for ", AnsiColor.RED)
        print_(displayedVersion, AnsiColor.RED, AnsiStyle.BOLD)
        print_(" was interrupted by user.", AnsiColor.RED)
        println_()
    }
}
