package io.github.recrafter.crafter.cli.commands.help

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.buildString
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.recrafter.crafter.cli.Fingerprint
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

    private val descriptionPrefix: String = buildString(Constants.Char.EM_DASH, Constants.Char.SPACE)

    override fun run(fingerprint: Fingerprint): String = script {
        println_()
        Logo.generate().lines().forEach { line ->
            println_(line + Constants.Char.SPACE)
        }
        println_()
        withCentering(cursorLine - 2) {
            val versionInfo = "CLI v${fingerprint.pluginVersion}"
            println_(versionInfo)
            println_(BoxDraw.HORIZONTAL.repeat(versionInfo.length))
        }
        println_()
        val exampleSignature = buildString {
            append("command".angled())
            append(Constants.Char.SPACE)
            append("arguments".squared())
        }
        println_("Usage: ${Cmd.of(fingerprint.scriptName, exampleSignature)}")
        println_()
        println_("Commands:")
        withPadding {
            printColumns(commandsProvider().map { columnLine(it.signature, descriptionPrefix + it.description) })
        }
        println_()
        println_("Arguments:")
        withPadding {
            printColumns(buildArgumentsSection())
        }
        println_()
        println_("Legend:")
        withPadding {
            printColumns(
                listOf(
                    columnLine(DEFAULT_OPTION_MARKER, descriptionPrefix + "default option (used when none specified)"),
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
                is StringArgument -> listOf(columnLine(argument.name, descriptionPrefix + argument.description))

                is EnumArgument -> buildList {
                    val optionOffset = Constants.Char.SPACE.toString()
                    val optionDescriptionPrefix = optionOffset + Constants.Char.CLOSING_ANGLE_BRACKET + optionOffset
                    add(columnLine())
                    add(columnLine(argument.name, descriptionPrefix + argument.description))
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
