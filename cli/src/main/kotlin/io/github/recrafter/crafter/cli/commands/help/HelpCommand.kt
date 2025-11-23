package io.github.recrafter.crafter.cli.commands.help

import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendSuffix
import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.diskria.kotlin.utils.extensions.wrapWithBrackets
import io.github.diskria.kotlin.utils.extensions.wrapWithDoubleQuote
import io.github.diskria.kotlin.utils.extensions.wrapWithSpace
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.commands.common.Command
import io.github.recrafter.crafter.cli.commands.common.SimpleCommand
import io.github.recrafter.crafter.cli.extensions.common.shellScript
import io.github.recrafter.crafter.cli.shell.ShellHelper
import kotlin.math.max

class HelpCommand(val logo: String, val commandsProvider: () -> List<Command<*>>) : SimpleCommand(
    name = "help",
    description = "Show this help",
    aliases = listOf(Constants.Char.ASTERISK.toString()),
) {
    override fun run(fingerprint: Fingerprint): String = shellScript {
        shellPrintln()
        val logoLines = logo.lines()
        logoLines.forEach { line ->
            shellPrintln(line.appendSuffix(Constants.Char.SPACE.toString()))
        }
        val logoWidth = logoLines.maxOf { it.length }
        val versionText = "CLI v${fingerprint.pluginVersion}".wrapWithSpace().wrapWithBrackets(BracketsType.SQUARE)
        val leftPadding = max((logoWidth - versionText.length) / 2, 0)
        val centeredVersionLine = Constants.Char.SPACE.repeat(leftPadding) + versionText

        val signatureExample = buildString {
            append("command".wrapWithBrackets(BracketsType.ANGLE))
            append(Constants.Char.SPACE)
            append("arguments".wrapWithBrackets(BracketsType.SQUARE))
        }
        val terminalCommand = ShellHelper.terminalCommand(fingerprint.scriptName, signatureExample)

        shellPrintln()
        shellPrintln(centeredVersionLine)
        shellPrintln()
        shellPrintln("Usage: $terminalCommand")
        shellPrintln()
        shellPrintln("Available commands:")

        val commands = commandsProvider()
        val commandEntries = commands.map { it.signature }
        val paddingWidth = commandEntries.maxOf { it.length } + 2
        commands.zip(commandEntries).forEach { (command, signature) ->
            val padding = Constants.Char.SPACE.repeat(paddingWidth - signature.length)
            shellPrintln(
                signature + padding + Constants.Char.EM_DASH + Constants.Char.SPACE + command.description,
                padding = 2
            )
        }
        return@shellScript this
    }

    override fun complete(currentWordIndex: String, reply: (String) -> String): String =
        reply(commandsProvider().joinBySpace { it.name }.wrapWithDoubleQuote())
}
