package io.github.recrafter.crafter.cli.shell

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.primitives.repeat

object ShellHelper {

    fun terminalCommand(scriptName: String, arguments: String): String =
        buildString {
            append(Constants.Char.DOT)
            append(Constants.Char.SLASH)
            append(scriptName)
            append(Constants.Char.SPACE)
            append(arguments)
        }

    fun gradleTaskCommand(command: String): String =
        terminalCommand("gradlew", command)

    fun runSequentially(commands: List<String>): String =
        commands.toList().joinToString(Constants.Char.AMPERSAND.repeat(2))
}
