package io.github.recrafter.crafter.helpers.shell

import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.generics.joinByNewLine
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.diskria.kotlin.utils.extensions.wrapWithBrackets
import io.github.diskria.kotlin.utils.extensions.wrapWithDoubleQuote
import io.github.recrafter.crafter.extensions.common.buildScript
import io.github.recrafter.crafter.helpers.shell.syntax.ShellCase
import io.github.recrafter.crafter.helpers.shell.syntax.ShellKeyword

object ShellHelper {

    const val BASH_SHEBANG: String = "#!/usr/bin/env bash"

    val AND: String = "&".repeat(2)
    val OR: String = Constants.Char.VERTICAL_BAR.repeat(2)

    fun variable(expression: String): String =
        "$${expression.wrapWithBrackets(BracketsType.CURLY)}".wrapWithDoubleQuote()

    fun variable(expression: Int): String =
        variable(expression.toString())

    fun arrayElement(array: String, index: String): String =
        variable(array + index.wrapWithBrackets(BracketsType.SQUARE))

    fun arrayElement(array: String, index: Int): String =
        arrayElement(array, index.toString())

    fun whenBy(variable: String, cases: List<ShellCase>): String = buildScript {
        append { "${ShellKeyword.CASE.token} ${variable(variable)} in" }
        withIndent {
            append {
                cases.joinByNewLine { it.script }
            }
        }
        append { ShellKeyword.CASE.end }
    }

    fun case(name: String, aliases: List<String> = emptyList(), builder: ScriptBuilder.() -> ScriptBuilder): ShellCase =
        ShellCase(name, aliases, buildScript(builder))

    fun echo(text: String = Constants.Char.EMPTY, red: Boolean = false): String =
        if (red) """echo -e "\033[0;31m$text\033[0m""""
        else "echo ${text.wrapWithDoubleQuote()}"

    fun echoRed(text: String = Constants.Char.EMPTY): String =
        echo(text, red = true)

    fun comment(text: String): String =
        buildString {
            append(Constants.Char.NUMBER_SIGN)
            append(Constants.Char.SPACE)
            append(text)
        }

    fun fail(): String =
        "exit 1"
}
