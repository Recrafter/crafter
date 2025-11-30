package io.github.recrafter.crafter.cli.bash.builder

import io.github.diskria.gradle.utils.extensions.common.requireGradle
import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.*
import io.github.diskria.kotlin.utils.extensions.common.buildString
import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.recrafter.crafter.cli.bash.syntax.BashKeyword
import io.github.recrafter.crafter.cli.extensions.common.Builder
import io.github.recrafter.crafter.cli.extensions.common.bashScript
import io.github.recrafter.crafter.cli.extensions.quoted
import io.github.recrafter.crafter.cli.extensions.rounded
import io.github.recrafter.crafter.cli.extensions.singleQuoted
import io.github.recrafter.crafter.cli.extensions.squared
import io.github.recrafter.crafter.cli.properties.stringVar
import org.gradle.internal.impldep.org.bouncycastle.asn1.BERBitString
import kotlin.math.max

@Suppress("unused", "FunctionName", "UnusedReceiverParameter", "LocalVariableName")
class BashScriptBuilder {

    val bash: Bash = Bash

    val currentLineIndex: Int
        get() = script.lines().lastIndex + 1

    private var script: String = Constants.Char.EMPTY
    private var indent: Int = 0
    private var printPadding: Int = 0
    private var printCenteringAnchorLength: Int? = null

    fun BashScriptBuilder.code(code: () -> String): BashScriptBuilder {
        script = script.appendFollowingIndent(code(), indent).trimIndent()
        return this
    }

    fun BashScriptBuilder.withIndent(
        indent: Int = DEFAULT_INDENT,
        builder: Builder<BashScriptBuilder>
    ): BashScriptBuilder {
        this.indent = indent
        code { bashScript(builder = builder) }
        this.indent = -indent
        return this
    }

    fun BashScriptBuilder.withPadding(
        padding: Int = DEFAULT_PADDING,
        builder: Builder<BashScriptBuilder>
    ): BashScriptBuilder {
        requireGradle(printCenteringAnchorLength == null) {
            "Padding cannot be applied while centering is active."
        }
        this.printPadding += padding
        builder(this)
        this.printPadding -= padding
        return this
    }

    fun BashScriptBuilder.withCentering(
        anchorLineIndex: Int,
        builder: Builder<BashScriptBuilder>
    ): BashScriptBuilder {
        requireGradle(printPadding == 0) {
            "Centering cannot be applied while padding is active."
        }
        val line = script.lines()[anchorLineIndex].trim()
        requireGradle(line.startsWith(Commands.ECHO)) {
            "Expected line starting with ${Commands.ECHO.singleQuoted()}, but got: $line"
        }
        val startIndex = line.indexOfOrNull(Constants.Char.DOUBLE_QUOTE)
        val endIndex = line.lastIndexOfOrNull(Constants.Char.DOUBLE_QUOTE)
        requireGradle(startIndex != null && endIndex != null && startIndex != endIndex) {
            "Expected double quotes in line, but got: $line"
        }
        val innerText = line.substring(startIndex + 1, endIndex)
        val anchorLength = innerText.length
        requireGradle(anchorLength > 0) {
            "Anchor line too short: $innerText"
        }
        printCenteringAnchorLength = anchorLength
        builder(this)
        printCenteringAnchorLength = null
        return this
    }

    fun BashScriptBuilder.shebang(): BashScriptBuilder =
        code { "#!/usr/bin/env bash" }

    @Suppress("SpellCheckingInspection")
    fun BashScriptBuilder.errorOptions(
        onExit: Boolean = true,
        onUnset: Boolean = true,
        onPipeFail: Boolean = true,
    ): BashScriptBuilder =
        code {
            requireGradle(onExit || onUnset || onPipeFail) {
                "At least one error option is required."
            }
            buildString {
                append("set -")
                if (onExit) {
                    append("e")
                }
                if (onUnset) {
                    append("u")
                }
                if (onPipeFail) {
                    append("o pipefail")
                }
            }
        }

    fun BashScriptBuilder.setWorkingDirectory(path: String): BashScriptBuilder =
        run_(Commands.CD, path.quoted())

    fun BashScriptBuilder.comment(text: String): BashScriptBuilder =
        code { buildString(Constants.Char.NUMBER_SIGN, Constants.Char.SPACE, text) }

    fun initStringVar(name: String, value: String): StringVar {
        code { bash.initStringVar(name, value) }
        return StringVar(name)
    }

    fun initIntVar(name: String, value: String): IntVar {
        code { bash.initIntVar(name, value) }
        return IntVar(name)
    }

    fun initArrayVar(name: String, value: String? = null): ArrayVar {
        code { bash.initArrayVar(name, value) }
        return ArrayVar(name)
    }

    fun initMapVar(name: String, map: Map<String, String>): MapVar {
        code { "declare -A $name" }
        map.toList().sortedBy { it.first }.forEach { (key, value) ->
            putToMap(name, key, value.quoted())
        }
        return MapVar(name)
    }

    fun BashScriptBuilder.setStringVarValue(stringVar: StringVar, value: String): BashScriptBuilder =
        code { bash.initStringVar(stringVar.name, value) }

    fun BashScriptBuilder.setIntVarValue(intVar: IntVar, value: String): BashScriptBuilder =
        code { bash.initIntVar(intVar.name, value) }

    fun BashScriptBuilder.setIntVarValue(intVar: IntVar, value: Int): BashScriptBuilder =
        setIntVarValue(intVar, value.toString())

    fun BashScriptBuilder.setArrayVarValue(arrayVar: ArrayVar, value: String): BashScriptBuilder =
        code { bash.initArrayVar(arrayVar.name, value, quote = true) }

    fun BashScriptBuilder.addToArrayVar(arrayVar: ArrayVar, element: StringVar): BashScriptBuilder =
        code { arrayVar.add(element.quotedValue) }

    fun BashScriptBuilder.incrementIntVarValue(intVar: IntVar): BashScriptBuilder =
        code { intVar.increment() }

    fun BashScriptBuilder.putToMap(name: String, key: String, value: String): BashScriptBuilder =
        code { name + key.squared() + Constants.Char.EQUAL_SIGN + value }

    fun BashScriptBuilder.when_(by: String, builder: Builder<CaseChainBuilder>): BashScriptBuilder =
        code { CaseChainBuilder(by).builder().build() }

    fun BashScriptBuilder.ifBlock(builder: Builder<IfChainBuilder>): BashScriptBuilder =
        code { IfChainBuilder().builder().build() }

    fun BashScriptBuilder.fun_(name: String, builder: Builder<BashScriptBuilder>): String {
        code {
            buildString {
                append(name)
                append(Constants.Char.OPENING_ROUND_BRACKET)
                append(Constants.Char.CLOSING_ROUND_BRACKET)
                append(Constants.Char.SPACE)
                append(Constants.Char.OPENING_CURLY_BRACKET)
            }
        }
        withIndent(builder = builder)
        code { buildString(Constants.Char.CLOSING_CURLY_BRACKET) }
        return name
    }

    fun BashScriptBuilder.while_(condition: Condition, builder: Builder<BashScriptBuilder>): BashScriptBuilder {
        code { "while $condition; do" }
        withIndent(builder = builder)
        code { "done" }
        return this
    }

    fun BashScriptBuilder.foreach_(
        arrayVar: ArrayVar,
        iteration: BashScriptBuilder.(StringVar) -> BashScriptBuilder
    ): BashScriptBuilder {
        val it_ = StringVar("it")
        code { "for ${it_.name} in ${arrayVar.iterator_().quoted()}; do" }
        withIndent {
            iteration(it_)
        }
        code { "done" }
        return this
    }

    fun BashScriptBuilder.return_(): BashScriptBuilder =
        code { BashKeyword.RETURN.token }

    fun BashScriptBuilder.break_(): BashScriptBuilder =
        code { BashKeyword.BREAK.token }

    fun BashScriptBuilder.run_(command: String, arguments: String): BashScriptBuilder =
        code { bash.run(command, arguments) }

    fun BashScriptBuilder.run_(command: String, argument: StringVar): BashScriptBuilder =
        code { bash.run(command, argument.toString()) }

    fun BashScriptBuilder.run_(commands: List<String>): BashScriptBuilder =
        code { bash.run_(commands) }

    fun BashScriptBuilder.createDirectory(path: String, recursive: Boolean = false): BashScriptBuilder =
        run_(Commands.MKDIR, buildString {
            if (recursive) {
                append("-p ")
            }
            append(path.quoted())
        })

    fun BashScriptBuilder.spaces(count: Int): String =
        Constants.Char.SPACE.repeat(count)

    fun BashScriptBuilder.println_(
        text: String = Constants.Char.EMPTY,
        color: AnsiColor? = null,
        style: AnsiStyle? = null,
    ): BashScriptBuilder =
        code { bash.print(text, color, style, addNewLine = true) }

    fun BashScriptBuilder.println_(
        text: StringVar,
        color: AnsiColor? = null,
        style: AnsiStyle? = null,
    ): BashScriptBuilder =
        println_(text.toString(), color, style)

    fun BashScriptBuilder.print_(
        text: String = Constants.Char.EMPTY,
        color: AnsiColor? = null,
        style: AnsiStyle? = null,
    ): BashScriptBuilder =
        code { bash.print(text, color, style, addNewLine = false) }

    fun BashScriptBuilder.clearLastLine(): BashScriptBuilder =
        code { listOf(Commands.ECHO, "-e -n", "\\r${ESC}K".quoted()).joinBySpace() }

    fun BashScriptBuilder.setCursorVisible(isVisible: Boolean): BashScriptBuilder {
        code { bash.print(bash.getCursorVisibilityCode(isVisible)) }
        if (!isVisible) {
            code { """trap "echo -e '${bash.getCursorVisibilityCode(true)}'" EXIT""" }
        }
        return this
    }

    fun BashScriptBuilder.printWarning(message: String, style: AnsiStyle? = null): BashScriptBuilder =
        println_(message, color = AnsiColor.YELLOW, style = style)

    fun BashScriptBuilder.printError(message: String, style: AnsiStyle? = null): BashScriptBuilder =
        println_(message, color = AnsiColor.RED, style = style)

    fun BashScriptBuilder.sleep(time: Float): BashScriptBuilder =
        run_("sleep", time.toString())

    fun BashScriptBuilder.exit(code: Int): BashScriptBuilder =
        code { "exit $code" }

    fun BashScriptBuilder.throw_(exceptionMessage: String? = null): BashScriptBuilder {
        exceptionMessage?.let { printError(it) }
        exit(ERROR_CODE)
        return this
    }

    fun BashScriptBuilder.onInterrupt(callback: Builder<BashScriptBuilder>): BashScriptBuilder {
        val trapFunction = fun_("interrupt") {
            callback()
            exit(130)
        }
        run_("trap", "$trapFunction SIGINT")
        return this
    }

    fun BashScriptBuilder.deleteRecursively(path: String): BashScriptBuilder {
        run_(Commands.RM, "-r ${path.quoted()}")
        return this
    }

    fun BashScriptBuilder.watchFileLines(
        path: StringVar,
        pid: StringVar? = null,
        action: BashScriptBuilder.(StringVar) -> BashScriptBuilder
    ): BashScriptBuilder {
        val line by stringVar()
        code { "while IFS= read -r ${line.name}; do" }
        withIndent {
            action(line)
        }
        code {
            buildString {
                append("done < <(tail -F ${path.quotedValue} ")
                pid?.let {
                    append("--pid=${it.quotedValue} ")
                }
                append("-n 0)")
            }
        }
        return this
    }

    //region Bash

    object Bash {
        val conditions: Conditions = Conditions
        val completion: Completion = Completion
    }

    fun Bash.initStringVar(name: String, value: String, quote: Boolean = true): String =
        buildString {
            append(name)
            append(Constants.Char.EQUAL_SIGN)
            append(
                if (quote) value.quoted()
                else value
            )
        }

    fun Bash.initIntVar(name: String, value: String): String =
        name + Constants.Char.EQUAL_SIGN + value

    fun Bash.initIntVar(name: String, value: Int): String =
        name + Constants.Char.EQUAL_SIGN + value.toString()

    fun Bash.initArrayVar(name: String, contents: String? = null, quote: Boolean = false): String =
        initStringVar(
            name,
            if (quote) contents.orEmpty().quoted().rounded()
            else contents.orEmpty().rounded(),
            quote = false
        )

    fun Bash.getScriptLocation(): String =
        bash.getAbsolutePath(bash.getDirectoryName(getArrayVar("BASH_SOURCE").getElement(0)).command).command

    fun Bash.run(command: String, arguments: String? = null): String =
        buildString {
            append(command)
            arguments?.let {
                append(Constants.Char.SPACE)
                append(it)
            }
        }

    fun Bash.run_(commands: List<String>): String =
        commands.joinToString(Constants.Char.AMPERSAND.repeat(2).wrapWithSpace())

    fun Bash.run_(vararg commands: String): String =
        run_(commands.toList())

    fun Bash.source(path: String): String =
        run(Commands.SOURCE, path.quoted())

    fun Bash.getCursorVisibilityCode(isVisible: Boolean): String =
        "$ESC?25" + if (isVisible) "h" else "l"

    fun Bash.print(
        text: String = Constants.Char.EMPTY,
        color: AnsiColor? = null,
        style: AnsiStyle? = null,
        addNewLine: Boolean = true,
    ): String {
        val spacesCount = printCenteringAnchorLength
            ?.let { max((it - text.length) / 2, 0) }
            ?: printPadding
        val finalText = if (color != null || style != null) {
            val esc = """\033["""
            buildString {
                append(esc)
                style?.let {
                    append(it.code)
                    append(Constants.Char.SEMICOLON)
                }
                color?.let {
                    append(it.code)
                }
                append("m")
                append(text)
                append(esc)
                append(AnsiStyle.RESET.code)
                append("m")
            }
        } else text
        val echoFlags = buildString {
            append("-e")
            if (!addNewLine) {
                append(" -n")
            }
        }
        return listOfNotNull(
            Commands.ECHO,
            echoFlags,
            (spaces(spacesCount) + finalText).quoted()
        ).joinBySpace()
    }

    fun Bash.getIntVar(name: String): IntVar =
        IntVar(name)

    fun Bash.getStringVar(name: String): StringVar =
        StringVar(name)

    fun Bash.getArrayVar(name: String): ArrayVar =
        ArrayVar(name)

    fun Bash.getMapVar(name: String): MapVar =
        MapVar(name)

    fun Bash.getScriptArgument(index: Int, default: String? = null): String =
        getStringVar(index.toString()).getValue(default)

    //endregion

    //region Lambda

    fun Bash.lambda(command: String, arguments: String? = null): Lambda =
        Lambda(Constants.Char.DOLLAR + run(command, arguments).rounded())

    fun Bash.nowDate(format: String = "+%Y-%m-%d_%H-%M-%S"): Lambda =
        lambda(Commands.DATE, format)

    fun Bash.getDirectoryName(path: String): Lambda =
        lambda(Commands.DIRNAME, path.quoted())

    fun Bash.getAbsolutePath(relativePath: String): Lambda =
        lambda(Commands.CD, run_(relativePath.quoted(), Commands.PWD))

    fun Bash.readFile(path: String): Lambda =
        lambda(Commands.CAT, path.quoted())

    fun Bash.getWorkingDirectory(): Lambda =
        lambda(Commands.PWD)

    fun Bash.getTerminalWidth(): Lambda =
        lambda("tput", "cols")

    fun Bash.readFileLastLine(path: String): Lambda =
        lambda("tail", "-n 1 ${path.quoted()}")

    fun Bash.math(expression: String): Lambda =
        lambda("expr", expression)

    //endregion

    //region Completion

    fun Completion.enableZshSupport(): String =
        bash.run_("autoload -U bashcompinit", "bashcompinit")

    val Completion.typingWordIndex: IntVar
        get() = bash.getIntVar(TYPING_WORD_INDEX)

    fun Completion.reply(reply: String): Lambda =
        Lambda(bash.initArrayVar(REPLY_ARRAY, generate(reply).command, quote = false))

    fun Completion.getWord(index: Int): String =
        getWord(index.toString())

    private fun Completion.generate(reply: String): Lambda =
        bash.lambda(GENERATE_COMMAND, "-W $reply -- ${getWord(TYPING_WORD_INDEX)}")

    private fun Completion.getWord(index: String): String =
        bash.getMapVar(WORDS).getValue(index)

    //endregion

    //region Column

    fun BashScriptBuilder.columnLine(left: String? = null, right: String? = null): ColumnLine =
        ColumnLine(left.orEmpty(), right.orEmpty())

    fun BashScriptBuilder.printColumns(
        lines: List<ColumnLine>,
        margin: Int = DEFAULT_COLUMN_MARGIN
    ): BashScriptBuilder {
        val paddingWidth = lines.maxOf { it.left.length } + margin
        lines.forEach { (left, right) ->
            println_(left + spaces(paddingWidth - left.length) + right)
        }
        return this
    }

    //endregion

    override fun toString(): String =
        script

    companion object {
        private const val DEFAULT_INDENT: Int = 4
        private const val DEFAULT_PADDING: Int = 2
        private const val DEFAULT_COLUMN_MARGIN: Int = 2

        private const val ERROR_CODE: Int = 1

        private const val ESC: String = """\033["""

        fun getVariableReference(name: String, default: String? = null, quote: Boolean = true): String {
            val reference = buildString {
                append(Constants.Char.DOLLAR)
                val curlyInner = buildString {
                    append(name)
                    default?.let {
                        append(Constants.Char.COLON)
                        append(Constants.Char.HYPHEN)
                        append(it)
                    }
                }
                append(curlyInner.wrapWithBrackets(BracketsType.CURLY))
            }
            return if (quote) reference.quoted() else reference
        }
    }
}
