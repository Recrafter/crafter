package io.github.recrafter.crafter.cli.shell

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
import io.github.recrafter.crafter.cli.shell.arguments.ArgumentReference
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

    fun ShellScriptBuilder.getVar(name: String): String =
        getShellVar(name)

    fun ShellScriptBuilder.getScriptArgument(index: Int): String =
        getVar(index.toString())

    fun ShellScriptBuilder.getArrayValue(array: String, index: String): String =
        getVar(array + index.wrapWithBrackets(BracketsType.SQUARE))

    fun ShellScriptBuilder.getArrayValue(array: String, index: Int): String =
        getArrayValue(array, index.toString())

    fun ShellScriptBuilder.getLocalVar(name: String): String =
        getShellVar(name, isLocal = true)

    fun ShellScriptBuilder.isFileExists(path: String): String =
        "-f ${path.wrapWithDoubleQuote()}"

    fun ShellScriptBuilder.isEmpty(reference: ArgumentReference): String =
        "-z ${reference.name}"

    fun ShellScriptBuilder.isNotEmpty(reference: ArgumentReference): String =
        "-n ${reference.name}"

    fun ShellScriptBuilder.shebang(): ShellScriptBuilder =
        code { "#!/usr/bin/env bash" }

    fun ShellScriptBuilder.comment(text: String): ShellScriptBuilder =
        code { "# $text" }

    fun ShellScriptBuilder.initVar(name: String, value: String, quote: Boolean = false): ShellScriptBuilder =
        code { sh.initVar(name, value, quote) }

    fun ShellScriptBuilder.initLocalVar(name: String, value: String): ShellScriptBuilder =
        initVar(name, value)

    fun ShellScriptBuilder.initArray(name: String, contents: String? = Constants.Char.EMPTY): ShellScriptBuilder =
        initVar(name, "($contents)")

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

    fun ShellScriptBuilder.declareLocalVar(name: String, initValue: String? = null): ShellScriptBuilder {
        require(name.lowercase() == name) { "Shell local variable names must be lowercase and single-word." }
        code { "local $name" }
        initValue?.let { code { "$name=$it" } }
        return this
    }

    fun ShellScriptBuilder.shellIfThen(vararg ifs: ShellIf): ShellScriptBuilder =
        code {
            val list = ifs.toList()
            list.withIndex().joinByNewLine { (index, current) ->
                current.generateScript(
                    previousIf = list.getOrNull(index - 1),
                    nextIf = list.getOrNull(index + 1)
                )
            }
        }

    fun ShellScriptBuilder.shellWhen(variable: String, cases: List<ShellCase>): ShellScriptBuilder {
        code { "${ShellKeyword.CASE.token} ${getLocalVar(variable)} in" }
        withIndent {
            code { cases.joinByNewLine { it.script } }
        }
        code { ShellKeyword.CASE.end }
        return this
    }

    fun ShellScriptBuilder.shellFun(
        name: String,
        builder: ShellScriptBuilder.() -> ShellScriptBuilder
    ): ShellScriptBuilder {
        code { "$name() {" }
        withIndent(builder = builder)
        code { "}" }
        return this
    }

    fun Sh.initVar(name: String, value: String, quote: Boolean = false): String =
        "$name=" + value.modifyIf(quote) { it.wrapWithDoubleQuote() }

    fun Sh.initArray(name: String, contents: String? = Constants.Char.EMPTY): String =
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

    override fun toString(): String =
        script

    object Sh

    companion object {
        private const val DEFAULT_INDENT: Int = 4

        fun getShellVar(name: String, isLocal: Boolean = false): String {
            require(isLocal || name.uppercase() == name) { "Shell variable names must be uppercase" }
            return "$${name.wrapWithBrackets(BracketsType.CURLY)}".wrapWithDoubleQuote()
        }
    }
}
