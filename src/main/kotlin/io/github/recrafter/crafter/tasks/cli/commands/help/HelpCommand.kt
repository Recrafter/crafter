package io.github.recrafter.crafter.tasks.cli.commands.help

import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendSuffix
import io.github.diskria.kotlin.utils.extensions.generics.joinByNewLine
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.diskria.kotlin.utils.extensions.wrapWithBrackets
import io.github.diskria.kotlin.utils.extensions.wrapWithSpace
import io.github.recrafter.crafter.extensions.common.buildScript
import io.github.recrafter.crafter.helpers.shell.ShellHelper
import io.github.recrafter.crafter.tasks.cli.Fingerprint
import io.github.recrafter.crafter.tasks.cli.commands.common.Command
import io.github.recrafter.crafter.tasks.cli.commands.common.SimpleCommand
import kotlin.math.max

class HelpCommand(val logo: String, val commands: () -> List<Command<*>>) : SimpleCommand(
    name = "help",
    description = "Show this help",
    aliases = listOf(Constants.Char.ASTERISK.toString()),
) {
    override fun run(fingerprint: Fingerprint): String = buildScript {
        val logoLines = logo.lines()
        append { ShellHelper.echo() }
        append {
            logoLines.joinByNewLine {
                ShellHelper.echo(it.appendSuffix(Constants.Char.SPACE.toString()))
            }
        }
        append {
            val logoWidth = logoLines.maxOf { it.length }
            val versionText = "CLI v${fingerprint.pluginVersion}".wrapWithSpace().wrapWithBrackets(BracketsType.SQUARE)
            val leftPadding = max((logoWidth - versionText.length) / 2, 0)
            val centeredVersionLine = Constants.Char.SPACE.repeat(leftPadding) + versionText
            """
            ${ShellHelper.echo()}
            ${ShellHelper.echo(centeredVersionLine)}
            ${ShellHelper.echo()}
            ${ShellHelper.echo("Usage: ./${fingerprint.scriptFileName} <command> [arguments]")}
            ${ShellHelper.echo()}
            ${ShellHelper.echo("Available commands:")}
            """
        }
        append {
            val commands = commands()
            val extraPadding = 2
            val commandEntries = commands.map { Constants.Char.SPACE.repeat(extraPadding) + it.schema }
            val paddingWidth = commandEntries.maxOf { it.length } + extraPadding
            commands.zip(commandEntries).joinByNewLine { (command, schema) ->
                val padding = Constants.Char.SPACE.repeat(paddingWidth - schema.length)
                ShellHelper.echo(schema + padding + Constants.Char.EM_DASH + Constants.Char.SPACE + command.description)
            }
        }
    }

    override fun complete(
        fingerprint: Fingerprint,
        currentWord: String,
        variants: (List<String>) -> String
    ): String =
        variants(commands().map { it.name })
}
