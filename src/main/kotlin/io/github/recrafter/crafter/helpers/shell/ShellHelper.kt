package io.github.recrafter.crafter.helpers.shell

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.modifyIf

object ShellHelper {

    fun terminalCommand(scriptName: String, arguments: String): String =
        buildString {
            append(Constants.Char.DOT)
            append(Constants.Char.SLASH)
            append(scriptName)
            append(Constants.Char.SPACE)
            append(arguments)
        }

    fun gradleCommand(command: String, quiet: Boolean = false): String =
        terminalCommand("gradlew", command.modifyIf(quiet) { "$it --quiet" })
}
