package io.github.recrafter.crafter.cli.bash.builder

import io.github.diskria.gradle.utils.extensions.common.requireGradle
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendFollowingIndent
import io.github.diskria.kotlin.utils.extensions.common.buildString
import io.github.diskria.kotlin.utils.extensions.common.className
import io.github.diskria.kotlin.utils.extensions.indexOfOrNull
import io.github.diskria.kotlin.utils.extensions.lastIndexOfOrNull
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.diskria.kotlin.utils.extensions.wrapWithSpace
import io.github.recrafter.crafter.cli.bash.BashCommand
import io.github.recrafter.crafter.cli.bash.BashKeyword
import io.github.recrafter.crafter.cli.bash.ExitCode
import io.github.recrafter.crafter.cli.bash.ansi.AnsiCodes
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor
import io.github.recrafter.crafter.cli.bash.ansi.AnsiStyle
import io.github.recrafter.crafter.cli.bash.ascii.ASCIICodes
import io.github.recrafter.crafter.cli.bash.completion.BashCompletion
import io.github.recrafter.crafter.cli.bash.conditions.BashCondition
import io.github.recrafter.crafter.cli.bash.conditions.BashConditions
import io.github.recrafter.crafter.cli.bash.conditions.BashConditions.exists
import io.github.recrafter.crafter.cli.bash.conditions.BashConditions.isDescriptorReady
import io.github.recrafter.crafter.cli.bash.conditions.BashConditions.isPidAlive
import io.github.recrafter.crafter.cli.bash.properties.CommandInputReference
import io.github.recrafter.crafter.cli.bash.properties.function
import io.github.recrafter.crafter.cli.bash.properties.intVar
import io.github.recrafter.crafter.cli.bash.properties.stringVar
import io.github.recrafter.crafter.cli.bash.references.FunctionReference
import io.github.recrafter.crafter.cli.bash.utils.ColumnLine
import io.github.recrafter.crafter.cli.bash.utils.ShellType
import io.github.recrafter.crafter.cli.bash.variables.*
import io.github.recrafter.crafter.cli.bash.zsh.ZshCommand
import io.github.recrafter.crafter.cli.bash.zsh.completion.ZshCompletion
import io.github.recrafter.crafter.cli.extensions.*
import io.github.recrafter.crafter.cli.extensions.common.Builder
import io.github.recrafter.crafter.cli.extensions.common.script
import io.github.recrafter.crafter.cli.extensions.common.spaced
import java.util.concurrent.TimeUnit
import kotlin.math.max

@Suppress("unused", "FunctionName", "UnusedReceiverParameter")
class ScriptBuilder {

    val bash: Bash = Bash
    val zsh: Zsh = Zsh
    val cursor: BashCursor = BashCursor

    val linesCount: Int
        get() = script.lines().size

    private var script: String = Constants.Char.EMPTY
    private var codeIndent: Int = 0
    private var printPadding: Int = 0
    private var printCenteringAnchorLength: Int? = null

    fun ScriptBuilder.code(code: () -> String): ScriptBuilder {
        script = script.appendFollowingIndent(code(), codeIndent).trimIndent()
        return this
    }

    fun ScriptBuilder.withIndent(
        indent: Int = DEFAULT_INDENT,
        builder: Builder<ScriptBuilder>
    ): ScriptBuilder {
        this.codeIndent = indent
        code { script(builder = builder) }
        this.codeIndent = -indent
        return this
    }

    fun ScriptBuilder.withPadding(padding: Int = DEFAULT_PADDING, builder: Builder<ScriptBuilder>): ScriptBuilder {
        requireGradle(printCenteringAnchorLength == null) {
            "Padding cannot be applied while centering is active."
        }
        this.printPadding += padding
        builder(this)
        this.printPadding -= padding
        return this
    }

    fun ScriptBuilder.withCentering(anchorLineIndex: Int, builder: Builder<ScriptBuilder>): ScriptBuilder {
        requireGradle(printPadding == 0) {
            "Centering cannot be applied while padding is active."
        }
        val line = script.lines()[anchorLineIndex].trim()
        requireGradle(line.startsWith(BashCommand.ECHO.command)) {
            "Expected line starting with ${BashCommand.ECHO.command.singleQuoted()}, but got: $line"
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

    fun ScriptBuilder.shebang(shellType: ShellType = ShellType.BASH): ScriptBuilder =
        code { "#!/usr/bin/env ${shellType.getName()}" }

    @Suppress("SpellCheckingInspection")
    fun ScriptBuilder.enableNullGlobbing(): ScriptBuilder =
        run_(BashCommand.SHOPT, "-s nullglob")

    @Suppress("SpellCheckingInspection")
    fun ScriptBuilder.errorOptions(
        onExit: Boolean = true,
        onUnset: Boolean = true,
        onPipeFail: Boolean = true,
    ): ScriptBuilder =
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

    fun initString(name: String, value: String = ""): StringVar {
        code { bash.setStringValue(name, value) }
        return bash.getString(name)
    }

    fun <E : Enum<E>> initEnum(name: String, value: E? = null): EnumVar<E> {
        code { bash.setEnumValue(name, value?.name?.lowercase().orEmpty()) }
        return bash.getEnum(name)
    }

    fun initInt(name: String, value: String): IntVar {
        code { bash.setIntValue(name, value) }
        return bash.getInt(name)
    }

    fun initInt(name: String, value: Int = 0): IntVar =
        initInt(name, value.toString())

    fun initBoolean(name: String, value: Boolean = false): BooleanVar {
        code { bash.setBooleanValue(name, value) }
        return bash.getBoolean(name)
    }

    fun initArray(name: String, value: String? = null): ArrayVar {
        code { bash.setArrayValue(name, value) }
        return bash.getArray(name)
    }

    fun initMap(name: String, map: Map<String, String>): MapVar {
        val mapVar = declareMap(name)
        map.toList().sortedBy { it.first }.forEach { (key, value) ->
            putToMapVar(mapVar, key, value.quoted())
        }
        return mapVar
    }

    fun ScriptBuilder.initArrayIndicesMap(array: ArrayVar): MapVar {
        val mapVar = declareMap(array.name + "_INDICES")
        val index by intVar()
        forEach_(array) { element ->
            putToMapVar(mapVar, element.quotedValue, index.value)
            incrementIntValue(index)
        }
        return mapVar
    }

    fun ScriptBuilder.notifyVarChanged(
        varSync: VarSync,
        value: String = bash.getString(varSync.varName).value
    ): ScriptBuilder {
        run_(BashCommand.ECHO, "${value.quoted()} >&$${varSync.descriptorVarName}")
        if (varSync.strategy == VarSyncStrategy.SINGLETON) {
            run_(BashCommand.EXEC, "${varSync.descriptorVarName.curled()}>&-")
        }
        return this
    }

    fun ScriptBuilder.checkVarUpdate(varSync: VarSync): ScriptBuilder {
        requireGradle(varSync.strategy != VarSyncStrategy.INPUT) {
            "${VarSync::class.className()} with ${VarSyncStrategy.INPUT} strategy doesn't supports checkVarUpdate()"
        }
        val descriptorData = initString(varSync.descriptorDataVarName)
        ifBlock {
            ifAll(
                bash.conditions.exists(varSync.descriptorPath),
                bash.conditions.isDescriptorReady(varSync.descriptorVarName, descriptorData.name),
            ) {
                code { bash.setStringValue(varSync.varName, descriptorData.quotedValue) }
                if (varSync.strategy == VarSyncStrategy.SINGLETON) {
                    deleteDirectory(bash.getString(varSync.fifoPathVarName))
                }
                return@ifAll this
            }
        }
        return this
    }

    fun ScriptBuilder.sendCommand(input: CommandInputReference, command: String): ScriptBuilder {
        notifyVarChanged(input.varSync, command)
        return this
    }

    fun ScriptBuilder.onNextVar(varSync: VarSync, action: Builder<ScriptBuilder>): ScriptBuilder {
        requireGradle(varSync.strategy == VarSyncStrategy.QUEUE) {
            "Only ${VarSync::class.className()} with ${VarSyncStrategy.QUEUE} strategy supports onNextVar()"
        }
        checkVarUpdate(varSync)
        ifBlock {
            val syncedVar = bash.getString(varSync.varName)
            if_(syncedVar.isNotEmpty()) {
                action()
                notifyVarChanged(varSync, "")
            }
        }
        return this
    }

    fun ScriptBuilder.setIntValue(int: IntVar, value: String): ScriptBuilder =
        code { bash.setIntValue(int.name, value) }

    fun ScriptBuilder.setIntValue(int: IntVar, value: Int): ScriptBuilder =
        setIntValue(int, value.toString())

    fun ScriptBuilder.setIntValue(int: IntVar, value: IntVar): ScriptBuilder =
        setIntValue(int, value.value)

    fun ScriptBuilder.setBooleanValue(boolean: BooleanVar, value: Boolean): ScriptBuilder =
        code { bash.setBooleanValue(boolean.name, value) }

    fun ScriptBuilder.setStringValue(string: StringVar, value: String): ScriptBuilder =
        code { bash.setStringValue(string.name, value) }

    fun ScriptBuilder.setStringValue(string: StringVar, value: StringVar): ScriptBuilder =
        setStringValue(string, value.value)

    fun <E : Enum<E>> ScriptBuilder.setEnumValue(enum: EnumVar<E>, value: E): ScriptBuilder =
        code { bash.setEnumValue(enum.name, value.name.lowercase()) }

    fun ScriptBuilder.setArrayValue(array: ArrayVar, value: String): ScriptBuilder =
        code { bash.setArrayValue(array.name, value) }

    fun ScriptBuilder.setArrayValue(array: ArrayVar, value: ArrayVar): ScriptBuilder =
        setArrayValue(array, value.iterator_())

    fun ScriptBuilder.reverseArray(array: ArrayVar): ArrayVar {
        val reversedArray = initArray(array.name + "_REVERSED")
        forEachIndexed(array, isReversed = true) { _, it ->
            addToArray(reversedArray, it)
        }
        return reversedArray
    }

    fun ScriptBuilder.incrementIntValue(int: IntVar): ScriptBuilder =
        code { int.increment() }

    fun ScriptBuilder.decrementIntValue(int: IntVar): ScriptBuilder =
        code { int.decrement() }

    fun ScriptBuilder.addToArray(array: ArrayVar, element: StringVar): ScriptBuilder =
        code { array.add(element.quotedValue) }

    fun ScriptBuilder.putToMapVar(map: MapVar, key: String, value: String): ScriptBuilder =
        code { map.name + key.squared() + Constants.Char.EQUAL_SIGN + value }

    fun initFunction(name: String, builder: Builder<ScriptBuilder>): FunctionReference {
        code {
            spaced(
                name + Constants.Char.OPENING_ROUND_BRACKET + Constants.Char.CLOSING_ROUND_BRACKET,
                Constants.Char.OPENING_CURLY_BRACKET,
            )
        }
        withIndent(builder = builder)
        code { buildString(Constants.Char.CLOSING_CURLY_BRACKET) }
        return FunctionReference(name)
    }

    fun initCommandInput(name: String): CommandInputReference =
        CommandInputReference(initVarSync(name, VarSyncStrategy.INPUT))

    fun ScriptBuilder.callFunction(function: FunctionReference): ScriptBuilder =
        code { function.name }

    fun ScriptBuilder.return_(): ScriptBuilder =
        code { BashKeyword.RETURN.token }

    fun ScriptBuilder.while_(
        condition: String,
        background: Boolean = false,
        source: String? = null,
        builder: Builder<ScriptBuilder>
    ): ScriptBuilder {
        code { spaced(BashKeyword.WHILE, condition.semicoloned(), BashKeyword.DO) }
        withIndent(builder = builder)
        code {
            spaced(
                BashKeyword.DONE,
                source?.let { "< $it" },
                if (background) BACKGROUND_FLAG else null
            )
        }
        return this
    }

    fun ScriptBuilder.readLines(
        sourceFilePath: StringVar,
        background: Boolean = false,
        action: ScriptBuilder.(StringVar) -> ScriptBuilder
    ): ScriptBuilder {
        val line by stringVar()
        return while_(spaced("IFS=''", BashCommand.READ, "-r", line.name), source = sourceFilePath.quotedValue) {
            action(line)
        }
    }

    fun ScriptBuilder.readLines(
        sourceCommand: String,
        background: Boolean = false,
        action: ScriptBuilder.(StringVar) -> ScriptBuilder
    ): ScriptBuilder {
        val line by stringVar()
        return while_(
            spaced("IFS=''", BashCommand.READ, "-r", line.name), source = "<" + sourceCommand.rounded(),
            background = background
        ) {
            action(line)
        }
    }

    fun ScriptBuilder.while_(
        condition: BashCondition,
        background: Boolean = false,
        builder: Builder<ScriptBuilder>
    ): ScriptBuilder =
        while_(condition.expression, background, builder = builder)

    fun ScriptBuilder.loop(
        background: Boolean = false,
        builder: Builder<ScriptBuilder>
    ): ScriptBuilder =
        while_(BashConditions.TRUE, background, builder)

    fun ScriptBuilder.forEach_(
        array: ArrayVar,
        iteration: ScriptBuilder.(it: StringVar) -> ScriptBuilder
    ): ScriptBuilder {
        val it = StringVar(array.name + "_ELEMENT")
        val iterator = array.iterator_().quoted().semicoloned()
        code { spaced(BashKeyword.FOR, it.name, BashKeyword.IN, iterator, BashKeyword.DO) }
        withIndent {
            iteration(it)
        }
        code { BashKeyword.DONE.token }
        return this
    }

    fun ScriptBuilder.forEachIndexed(
        array: ArrayVar,
        isReversed: Boolean = false,
        iteration: ScriptBuilder.(i: IntVar, it: StringVar) -> ScriptBuilder
    ): ScriptBuilder {
        val indexVarName = array.name + "_INDEX"
        val elementVarName = array.name + "_ELEMENT"
        if (isReversed) {
            val i = initInt(indexVarName, array.size.minus(1))
            while_(i.isGreaterOrEqual(0)) {
                val it = initString(elementVarName, array.getElement(i))
                iteration(i, it)
                decrementIntValue(i)
            }
        } else {
            val i = initInt(indexVarName)
            while_(i.isLessThen(array.size)) {
                val it = initString(elementVarName, array.getElement(i))
                iteration(i, it)
                incrementIntValue(i)
            }
        }
        return this
    }

    fun ScriptBuilder.break_(): ScriptBuilder =
        code { BashKeyword.BREAK.token }

    fun ScriptBuilder.continue_(): ScriptBuilder =
        code { BashKeyword.CONTINUE.token }

    fun ScriptBuilder.when_(by: String, builder: Builder<CaseChainBuilder>): ScriptBuilder =
        code { CaseChainBuilder(by).builder().build() }

    fun ScriptBuilder.ifBlock(builder: Builder<IfChainBuilder>): ScriptBuilder =
        code { IfChainBuilder().builder().build() }

    fun ScriptBuilder.run_(command: BashCommand, arguments: String): ScriptBuilder =
        code { bash.run_(command, arguments) }

    fun ScriptBuilder.runSequentially(commands: List<String>): ScriptBuilder =
        code { bash.runSequentially(commands) }

    fun ScriptBuilder.runCommandInBackground(
        command: String,
        outputFilePath: StringVar,
        commandInput: CommandInputReference? = null,
    ): String {
        code {
            spaced(
                command,
                commandInput?.let { "<&$" + it.varSync.descriptorVarName },
                ">",
                outputFilePath,
                "2>&1",
                BACKGROUND_FLAG.toString(),
            )
        }
        return "$!"
    }

    fun ScriptBuilder.createDirectory(path: String, recursive: Boolean = true): ScriptBuilder =
        run_(BashCommand.MKDIR, buildString {
            if (recursive) {
                append("-p ")
            }
            append(path.quoted())
        })

    fun ScriptBuilder.createDirectory(path: StringVar, recursive: Boolean = true): ScriptBuilder =
        createDirectory(path.value, recursive)

    fun ScriptBuilder.copyDirectory(sourcePath: String, targetPath: String): ScriptBuilder {
        createDirectory(bash.getParentDirectoryPath(targetPath).command)
        run_(BashCommand.CP, spaced("-r", sourcePath.quoted(), targetPath.quoted()))
        return this
    }

    fun ScriptBuilder.setWorkingDirectory(path: String): ScriptBuilder =
        run_(BashCommand.CD, path.quoted())

    fun ScriptBuilder.println_(
        text: String = Constants.Char.EMPTY,
        color: AnsiColor? = null,
        style: AnsiStyle? = null,
    ): ScriptBuilder =
        code { bash.print(text, color, style, addNewLine = true) }

    fun ScriptBuilder.println_(
        text: StringVar,
        color: AnsiColor? = null,
        style: AnsiStyle? = null,
    ): ScriptBuilder =
        println_(text.value, color, style)

    fun ScriptBuilder.println_(
        array: ArrayVar,
        color: AnsiColor? = null,
        style: AnsiStyle? = null,
    ): ScriptBuilder =
        println_(array.value, color, style)

    fun ScriptBuilder.print_(
        text: String = Constants.Char.EMPTY,
        color: AnsiColor? = null,
        style: AnsiStyle? = null,
    ): ScriptBuilder =
        code { bash.print(text, color, style, addNewLine = false) }

    fun ScriptBuilder.print_(
        text: StringVar,
        color: AnsiColor? = null,
        style: AnsiStyle? = null,
    ): ScriptBuilder =
        print_(text.toString(), color, style)

    fun ScriptBuilder.clearLine(): ScriptBuilder =
        code { spaced(BashCommand.ECHO, "-en", (ASCIICodes.CARRIAGE_RETURN + AnsiCodes.escape("2K")).quoted()) }

    fun ScriptBuilder.sleep(count: Int, unit: TimeUnit): ScriptBuilder =
        run_(BashCommand.SLEEP, unit.toSeconds(count.toLong()).toString())

    fun ScriptBuilder.kill(pid: StringVar, force: Boolean = true): ScriptBuilder =
        run_(
            BashCommand.KILL, spaced(
                if (force) null else "-TERM",
                pid.value
            )
        )

    fun ScriptBuilder.wait(pid: StringVar): IntVar {
        run_(BashCommand.WAIT, pid.value)
        val exitCode by intVar("$?")
        return exitCode
    }

    fun ScriptBuilder.exit(code: Int): ScriptBuilder =
        run_(BashCommand.EXIT, code.toString())

    fun ScriptBuilder.trapOnInterrupt(function: FunctionReference): ScriptBuilder =
        run_(BashCommand.TRAP, spaced(function.name, "SIGINT"))

    fun ScriptBuilder.trapOnExit(command: String): ScriptBuilder =
        run_(BashCommand.TRAP, spaced(command.quoted(), "EXIT"))

    fun ScriptBuilder.trapOnExit(function: FunctionReference): ScriptBuilder =
        trapOnExit(function.name)

    fun ScriptBuilder.ensureProcessKilled(pid: StringVar): ScriptBuilder {
        ifBlock {
            if_(bash.conditions.isPidAlive(pid)) {
                kill(pid)
                wait(pid)
                return@if_ this
            }
        }
        return this
    }

    fun ScriptBuilder.moveDirectory(sourcePath: StringVar, targetPath: String): ScriptBuilder =
        run_(BashCommand.MV, spaced(sourcePath.quotedValue, targetPath.quoted()))

    fun ScriptBuilder.moveDirectory(sourcePath: StringVar, targetPath: StringVar): ScriptBuilder =
        moveDirectory(sourcePath, targetPath)

    fun ScriptBuilder.renameDirectory(oldPath: StringVar, newPath: String): ScriptBuilder =
        moveDirectory(oldPath, newPath)

    fun ScriptBuilder.renameDirectory(oldPath: StringVar, newPath: StringVar): ScriptBuilder =
        moveDirectory(oldPath, newPath)

    fun ScriptBuilder.deleteDirectory(path: String, recursive: Boolean = false): ScriptBuilder =
        run_(
            BashCommand.RM,
            spaced(
                if (recursive) "-r" else null,
                "-f",
                path.quoted()
            )
        )

    fun ScriptBuilder.deleteDirectory(path: StringVar, recursive: Boolean = false): ScriptBuilder =
        deleteDirectory(path.quotedValue, recursive)

    fun ScriptBuilder.deleteFile(path: StringVar): ScriptBuilder =
        deleteDirectory(path.quotedValue, false)

    fun ScriptBuilder.error_(): ScriptBuilder =
        exit(ExitCode.ERROR)

    fun ScriptBuilder.onInterrupt(callback: Builder<ScriptBuilder>): ScriptBuilder {
        val onInterruptFunction by function {
            callback()
            exit(ExitCode.INTERRUPT)
        }
        trapOnInterrupt(onInterruptFunction)
        return this
    }

    fun ScriptBuilder.onExit(callback: Builder<ScriptBuilder>): ScriptBuilder {
        val onExitFunction by function {
            callback()
        }
        trapOnExit(onExitFunction)
        return this
    }

    fun ScriptBuilder.runFileWatcherInBackground(
        path: StringVar,
        pid: StringVar? = null,
        action: ScriptBuilder.(StringVar) -> ScriptBuilder
    ): String {
        readLines(
            spaced(BashCommand.TAIL, "-F", path.quotedValue, pid?.let { "--pid=${it.quotedValue}" }, "-n", 0),
            true,
            action
        )
        return "$!"
    }

    fun ScriptBuilder.comment(text: String): ScriptBuilder =
        code { spaced(Constants.Char.NUMBER_SIGN, text) }

    fun ScriptBuilder.spaces(count: Int): String =
        Constants.Char.SPACE.repeat(count)

    fun String.semicoloned(): String =
        this + Constants.Char.SEMICOLON

    private fun declareMap(name: String): MapVar {
        val mapVar = MapVar(name)
        code { spaced(BashCommand.DECLARE, "-A", name) }
        return mapVar
    }

    //region Bash

    object Bash {
        val conditions: BashConditions = BashConditions
        val completion: BashCompletion = BashCompletion
    }

    fun Bash.setIntValue(name: String, value: String): String =
        name + Constants.Char.EQUAL_SIGN + value

    fun Bash.setIntValue(name: String, value: Int): String =
        setIntValue(name, value.toString())

    fun Bash.setBooleanValue(name: String, value: Boolean): String =
        setIntValue(name, BooleanVar.from(value))

    fun Bash.setStringValue(name: String, value: String, quote: Boolean = true): String =
        buildString {
            append(name)
            append(Constants.Char.EQUAL_SIGN)
            append(
                if (quote) value.quoted()
                else value
            )
        }

    fun Bash.setEnumValue(name: String, value: String): String =
        setStringValue(name, value)

    fun Bash.setArrayValue(name: String, contents: String? = null): String =
        setStringValue(name, contents.orEmpty().unquoted().rounded(), quote = false)

    fun Bash.run_(command: BashCommand, arguments: String? = null): String =
        spaced(command.command, arguments)

    fun Bash.runSequentially(commands: List<String>): String =
        commands.joinToString(Constants.Char.AMPERSAND.repeat(2).wrapWithSpace())

    fun Bash.runSequentially(vararg commands: String): String =
        runSequentially(commands.toList())

    fun Bash.source(path: String): String =
        run_(BashCommand.SOURCE, path.quoted())

    fun Bash.getInt(name: String): IntVar =
        IntVar(name)

    fun Bash.getBoolean(name: String): BooleanVar =
        BooleanVar(name)

    fun Bash.getString(name: String): StringVar =
        StringVar(name)

    fun <E : Enum<E>> Bash.getEnum(name: String): EnumVar<E> =
        EnumVar(name)

    fun Bash.getArray(name: String): ArrayVar =
        ArrayVar(name)

    fun Bash.getMap(name: String): MapVar =
        MapVar(name)

    fun Bash.getScriptArgument(index: Int, default: String? = null): String =
        getString(index.toString()).getValue(default)

    fun Bash.getScriptLocation(): String =
        bash.getAbsolutePath(bash.getParentDirectoryPath(getArray("BASH_SOURCE").getElement(0)).command).command

    fun Bash.isZsh(): BashCondition =
        bash.getString("ZSH_VERSION").isNotEmpty()

    private fun Bash.print(
        text: String = Constants.Char.EMPTY,
        color: AnsiColor? = null,
        style: AnsiStyle? = null,
        addNewLine: Boolean = true,
    ): String {
        val finalText = if (color != null || style != null) {
            buildString {
                append(AnsiCodes.ESCAPE)
                style?.let {
                    append(it.code.toString().semicoloned())
                }
                color?.let {
                    append(it.code)
                }
                append("m")
                append(text)
                append(AnsiCodes.ESCAPE)
                append(AnsiStyle.RESET.code)
                append("m")
            }
        } else text
        val spacesCount = printCenteringAnchorLength
            ?.let { max((it - finalText.length) / 2, 0) }
            ?: printPadding
        val echoFlags = buildString {
            append("-e")
            if (!addNewLine) {
                append(" -n")
            }
        }
        return spaced(BashCommand.ECHO, echoFlags, (spaces(spacesCount) + finalText).quoted())
    }

    //endregion

    //region BashLambda

    fun Bash.lambda(command: BashCommand, arguments: String? = null): BashLambda =
        BashLambda(Constants.Char.DOLLAR + run_(command, arguments).rounded())

    fun Bash.nowDate(format: String = "+%Y-%m-%d_%H-%M-%S"): BashLambda =
        lambda(BashCommand.DATE, format)

    fun Bash.getParentDirectoryPath(path: String): BashLambda =
        lambda(BashCommand.DIRNAME, path.quoted())

    fun Bash.getBasename(path: String): BashLambda =
        lambda(BashCommand.BASENAME, path.quoted())

    fun Bash.getBasename(path: StringVar): BashLambda =
        getBasename(path.value)

    fun Bash.getAbsolutePath(relativePath: String): BashLambda =
        lambda(BashCommand.REALPATH, relativePath.quoted())

    fun Bash.readFile(path: String): BashLambda =
        lambda(BashCommand.CAT, path.quoted())

    fun Bash.getWorkingDirectory(): BashLambda =
        lambda(BashCommand.PWD)

    //endregion

    //region BashCursor

    object BashCursor

    fun BashCursor.moveUp(steps: Int = 1): ScriptBuilder =
        run_(BashCommand.ECHO, spaced("-en", AnsiCodes.escape("${steps}A").quoted()))

    fun BashCursor.moveDown(steps: Int = 1): ScriptBuilder =
        run_(BashCommand.ECHO, spaced("-en", AnsiCodes.escape("${steps}B").quoted()))

    fun BashCursor.hide() {
        setVisible(false)
    }

    fun BashCursor.show() {
        setVisible(true)
    }

    private fun BashCursor.setVisible(isVisible: Boolean) {
        code { bash.print(getCursorVisibilityCode(isVisible), addNewLine = false) }
    }

    private fun BashCursor.getCursorVisibilityCode(isVisible: Boolean): String =
        AnsiCodes.escape("?25" + if (isVisible) "h" else "l")

    //endregion

    //region BashCompletion

    val BashCompletion.typingWordIndex: IntVar
        get() = bash.getInt(TYPING_WORD_INDEX)

    fun BashCompletion.reply(reply: String): BashLambda =
        BashLambda(bash.setArrayValue(REPLY_ARRAY, generate(reply).command))

    fun BashCompletion.getWord(index: Int): String =
        getWord(index.toString())

    private fun BashCompletion.generate(reply: String): BashLambda =
        bash.lambda(GENERATE_COMMAND, "-W $reply -- ${getWord(TYPING_WORD_INDEX)}")

    private fun BashCompletion.getWord(index: String): String =
        bash.getMap(WORDS).getValue(index)

    //endregion

    //region Zsh

    object Zsh {
        val completion: ZshCompletion = ZshCompletion
    }

    fun Zsh.run_(command: ZshCommand, arguments: String? = null): String =
        spaced(command, arguments)

    fun Zsh.runSequentially(commands: List<String>): String =
        commands.joinToString(Constants.Char.AMPERSAND.repeat(2).wrapWithSpace())

    fun Zsh.runSequentially(vararg commands: String): String =
        runSequentially(commands.toList())

    //endregion

    //region ZshCompletion

    fun ZshCompletion.enableBashCompatibility(): String =
        zsh.runSequentially(
            spaced(ZshCommand.AUTOLOAD, "-U", ZshCommand.BASHCOMPINIT),
            ZshCommand.BASHCOMPINIT.command,
        )

    //endregion

    //region Column

    fun ScriptBuilder.columnLine(left: String? = null, right: String? = null): ColumnLine =
        ColumnLine(left.orEmpty(), right.orEmpty())

    fun ScriptBuilder.printColumns(lines: List<ColumnLine>, margin: Int = DEFAULT_COLUMN_MARGIN): ScriptBuilder {
        val paddingWidth = lines.maxOf { it.left.length } + margin
        lines.forEach { (left, right) ->
            println_(left + spaces(paddingWidth - left.length) + right)
        }
        return this
    }

    //endregion

    override fun toString(): String = script

    companion object {
        private const val DEFAULT_INDENT: Int = 4
        private const val DEFAULT_PADDING: Int = 2
        private const val DEFAULT_COLUMN_MARGIN: Int = 2

        private const val BACKGROUND_FLAG: Char = Constants.Char.AMPERSAND
    }
}

fun ScriptBuilder.initVarSync(
    int: IntVar,
    strategy: VarSyncStrategy = VarSyncStrategy.SINGLETON
): VarSync =
    initVarSync(int.name, strategy)

fun ScriptBuilder.initVarSync(
    boolean: BooleanVar,
    strategy: VarSyncStrategy = VarSyncStrategy.SINGLETON
): VarSync =
    initVarSync(boolean.name, strategy)

fun ScriptBuilder.initVarSync(
    string: StringVar,
    strategy: VarSyncStrategy = VarSyncStrategy.SINGLETON
): VarSync =
    initVarSync(string.name, strategy)

fun ScriptBuilder.initVarSync(
    enum: EnumVar<*>,
    strategy: VarSyncStrategy = VarSyncStrategy.SINGLETON
): VarSync =
    initVarSync(enum.name, strategy)

private fun ScriptBuilder.initVarSync(name: String, strategy: VarSyncStrategy): VarSync {
    val varSync = VarSync(name, strategy)
    val path = initString(varSync.fifoPathVarName, varSync.fifoPath)
    deleteDirectory(path)
    run_(BashCommand.MKFIFO, path.quotedValue)
    run_(BashCommand.EXEC, "${varSync.descriptorVarName.curled()}<>${path.quotedValue}")
    return varSync
}
