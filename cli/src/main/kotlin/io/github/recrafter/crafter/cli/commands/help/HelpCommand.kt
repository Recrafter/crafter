package io.github.recrafter.crafter.cli.commands.help

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor
import io.github.recrafter.crafter.cli.bash.ansi.AnsiStyle
import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.bash.api.commands.AbstractCLICommand
import io.github.recrafter.crafter.cli.bash.api.commands.NoArgumentsCommand
import io.github.recrafter.crafter.cli.bash.api.commands.arguments.EnumArgument
import io.github.recrafter.crafter.cli.bash.api.commands.arguments.StringArgument
import io.github.recrafter.crafter.cli.bash.ascii.BoxDraw
import io.github.recrafter.crafter.cli.bash.utils.Cmd
import io.github.recrafter.crafter.cli.bash.utils.ColumnLine
import io.github.recrafter.crafter.cli.extensions.angled
import io.github.recrafter.crafter.cli.extensions.common.script
import io.github.recrafter.crafter.cli.extensions.common.withScript
import io.github.recrafter.crafter.cli.extensions.squared
import io.github.recrafter.crafter.cli.logo.Logo

@CLICommand(name = HelpCommand.COMMAND_NAME, description = "Show this message")
class HelpCommand(val commandsProvider: () -> List<AbstractCLICommand<*>>) : NoArgumentsCommand() {

    override fun run(fingerprint: Fingerprint): String = script {
        println_()
        val logoLines = Logo.generate().lines()
        logoLines.forEachIndexed { index, line ->
            println_(
                line + Constants.Char.SPACE,
                AnsiColor.YELLOW,
                if (index == 0) AnsiStyle.BOLD else null
            )
        }
        println_()
        withCentering(cursorLine - 2) {
            val versionInfo = "CLI v${fingerprint.pluginVersion}"
            println_(versionInfo, AnsiColor.MAGENTA, AnsiStyle.BOLD)
            println_(BoxDraw.HORIZONTAL.repeat(versionInfo.length), AnsiColor.MAGENTA, AnsiStyle.BOLD)
        }
        println_()
        val exampleSignature = buildString {
            append("command".angled())
            append(Constants.Char.SPACE)
            append("arguments".squared())
        }
        print_("Usage: ", AnsiColor.GREEN, AnsiStyle.BOLD)
        println_(Cmd.of(fingerprint.scriptName, exampleSignature))
        println_()
        println_("Commands:", AnsiColor.GREEN, AnsiStyle.BOLD)
        withPadding {
            printColumns(commandsProvider().map { columnLine(it.signature, it.description) })
        }
        println_()
        println_("Arguments:", AnsiColor.GREEN, AnsiStyle.BOLD)
        withPadding {
            printColumns(buildArgumentsSection())
        }
        println_()
        println_("Legend:", AnsiColor.GRAY, AnsiStyle.BOLD)
        withPadding {
            printColumns(
                listOf(
                    columnLine(DEFAULT_OPTION_MARKER, "default option (used when none specified)"),
                )
            )
        }
    }

    private fun buildArgumentsSection(): List<ColumnLine> = withScript {
        val commands = commandsProvider()
        val allArguments = commands.flatMap { it.arguments }
        val uniqueArguments = allArguments.distinctBy { it.name }
        val argumentFrequency = allArguments.map { it.name }.groupingBy { it }.eachCount()
        val (stringArguments, enumArguments) = uniqueArguments.partition { it is StringArgument }
        val arguments = stringArguments.sortedByDescending { argumentFrequency.getValue(it.name) } + enumArguments
        arguments.flatMap { argument ->
            when (argument) {
                is StringArgument -> listOf(columnLine(argument.name, argument.description))

                is EnumArgument -> buildList {
                    val optionOffset = Constants.Char.SPACE.toString()
                    val optionDescriptionPrefix = optionOffset + optionOffset
                    add(columnLine())
                    add(columnLine(argument.name, argument.description))
                    add(columnLine(optionOffset + BoxDraw.VERTICAL))
                    addAll(argument.options.mapIndexed { index, option ->
                        val left = buildString {
                            append(optionOffset)
                            append(
                                if (index == argument.options.lastIndex) BoxDraw.Corner.BOTTOM_LEFT
                                else BoxDraw.Connect.LEFT
                            )
                            append(BoxDraw.HORIZONTAL)
                            append(optionOffset)
                            append(option.name)
                            if (option == argument.defaultOption) {
                                append(DEFAULT_OPTION_MARKER)
                            }
                        }
                        val right = optionDescriptionPrefix + option.description
                        columnLine(left, right)
                    })
                }
            }
        }
    }

    companion object {
        const val COMMAND_NAME: String = "help"
        private val DEFAULT_OPTION_MARKER: String = marker(1)

        @Suppress("SameParameterValue")
        private fun marker(level: Int): String =
            Constants.Char.ASTERISK.repeat(level)
    }
}
