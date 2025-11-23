package io.github.recrafter.crafter.cli.shell

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendFollowingIndent
import io.github.diskria.kotlin.utils.extensions.common.modifyIf
import io.github.diskria.kotlin.utils.extensions.generics.joinByNewLine
import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.diskria.kotlin.utils.extensions.wrapWithBrackets
import io.github.diskria.kotlin.utils.extensions.wrapWithDoubleQuote
import io.github.recrafter.crafter.cli.extensions.common.shellScript
import io.github.recrafter.crafter.cli.shell.syntax.ShellCase
import io.github.recrafter.crafter.cli.shell.syntax.ShellIf
import io.github.recrafter.crafter.cli.shell.syntax.ShellKeyword

class ShellScriptBuilder {

    val sh: Sh = Sh

    private var script: String = Constants.Char.EMPTY
    private var offset: Int = 0

    fun ShellScriptBuilder.code(code: () -> String): ShellScriptBuilder {
        script = script.appendFollowingIndent(code(), offset).trimIndent()
        return this
    }

    fun ShellScriptBuilder.indentIn(steps: Int = DEFAULT_INDENT) {
        offset = steps
    }

    fun ShellScriptBuilder.indentOut(steps: Int = DEFAULT_INDENT) {
        offset = -steps
    }

    fun ShellScriptBuilder.withIndent(
        spaces: Int = DEFAULT_INDENT,
        builder: ShellScriptBuilder.() -> ShellScriptBuilder
    ): ShellScriptBuilder {
        indentIn(spaces)
        code { shellScript(builder) }
        indentOut(spaces)
        return this
    }

    fun ShellScriptBuilder.shebang(): ShellScriptBuilder =
        code { "#!/usr/bin/env bash" }

    fun ShellScriptBuilder.comment(text: String): ShellScriptBuilder =
        code { "# $text" }

    fun ShellScriptBuilder.initVar(name: String, value: String, quote: Boolean = false): ShellScriptBuilder =
        code { sh.initVar(name, value, quote) }

    fun ShellScriptBuilder.initMap(name: String, map: Map<String, String>): ShellScriptBuilder {
        require(name.uppercase() == name) { gradleError("Shell variable names must be uppercase") }
        code { "declare -A $name" }
        map.toList().sortedBy { it.first }.forEach { (key, value) -> putToMap(name, key, value) }
        return this
    }

    fun ShellScriptBuilder.putToMap(name: String, key: String, value: String): ShellScriptBuilder =
        initVar(name + key.wrapWithBrackets(BracketsType.SQUARE), value.wrapWithDoubleQuote())

    fun ShellScriptBuilder.initArray(name: String, contents: String = Constants.Char.EMPTY): ShellScriptBuilder =
        code { sh.initArray(name, contents) }

    fun ShellScriptBuilder.shellPrintln(
        text: String = Constants.Char.EMPTY,
        allowEscape: Boolean = false,
        padding: Int = 0,
    ): ShellScriptBuilder =
        code { sh.shellPrintln(text, allowEscape, padding) }

    fun ShellScriptBuilder.printErr(text: String = Constants.Char.EMPTY): ShellScriptBuilder =
        code { sh.printErr(text) }

    fun ShellScriptBuilder.throwException(message: String? = null): ShellScriptBuilder {
        message?.let { sh.printErr(it) }
        code { "exit 1" }
        return this
    }

    fun ShellScriptBuilder.buildIfThen(vararg ifs: ShellIf): ShellScriptBuilder =
        code {
            val list = ifs.toList()
            list.withIndex().joinByNewLine { (index, current) ->
                current.generateScript(
                    previousIf = list.getOrNull(index - 1),
                    nextIf = list.getOrNull(index + 1)
                )
            }
        }

    fun ShellScriptBuilder.buildWhen(variable: String, cases: List<ShellCase>): ShellScriptBuilder {
        code { "${ShellKeyword.CASE.token} ${sh.getLocalVar(variable)} in" }
        withIndent {
            code { cases.joinByNewLine { it.script } }
        }
        code { ShellKeyword.CASE.end }
        return this
    }

    fun ShellScriptBuilder.initLocalVar(name: String, initValue: String): String {
        code { "local $name" }
        code { "$name=$initValue" }
        return sh.getVar(name, quote = false)
    }

    fun ShellScriptBuilder.function(
        name: String,
        builder: ShellScriptBuilder.() -> ShellScriptBuilder
    ): String {
        code { "$name() {" }
        withIndent(builder = builder)
        code { "}" }
        return name
    }

    fun Sh.initVar(name: String, value: String, quote: Boolean = false): String =
        "$name=" + value.modifyIf(quote) { it.wrapWithDoubleQuote() }

    fun Sh.initArray(name: String, contents: String = Constants.Char.EMPTY): String =
        initVar(name, "($contents)")

    fun Sh.shellPrintln(text: String = Constants.Char.EMPTY, allowEscape: Boolean = false, padding: Int = 0): String =
        listOfNotNull(
            "echo",
            if (allowEscape) "-e" else null,
            (Constants.Char.SPACE.repeat(padding) + text).wrapWithDoubleQuote()
        ).joinBySpace()

    fun Sh.printErr(text: String = Constants.Char.EMPTY): String =
        shellPrintln("""\033[0;31m$text\033[0m""", allowEscape = true)

    fun Sh.invoke(command: String, arguments: String): String =
        "$($command $arguments)"

    fun Sh.readFile(path: String): String =
        invoke("cat", path.wrapWithDoubleQuote())

    fun Sh.isEmpty(variable: String): String =
        "-z $variable"

    fun Sh.isNotEmpty(variable: String): String =
        "-n $variable"

    fun Sh.isFileExists(path: String): String =
        "-f ${path.wrapWithDoubleQuote()}"

    fun Sh.getVar(name: String, quote: Boolean = true): String =
        getShellVar(name, quote = quote)

    fun Sh.getScriptArgument(index: Int): String =
        getVar(index.toString())

    fun Sh.getArrayValue(array: String, index: String): String =
        getVar(array + index.wrapWithBrackets(BracketsType.SQUARE))

    fun Sh.getArrayValue(array: String, index: Int): String =
        getArrayValue(array, index.toString())

    fun Sh.getLocalVar(name: String, quote: Boolean = true): String =
        getShellVar(name, isLocal = true, quote = quote)

    override fun toString(): String =
        script

    object Sh

    companion object {
        private const val DEFAULT_INDENT: Int = 4

        fun getShellVar(name: String, isLocal: Boolean = false, quote: Boolean = true): String {
            if (isLocal) {
                require(name.uppercase() != name) { gradleError("Shell local variable names must be lowercase") }
            }
            return "$${name.wrapWithBrackets(BracketsType.CURLY)}".modifyIf(quote) { it.wrapWithDoubleQuote() }
        }
    }
}
