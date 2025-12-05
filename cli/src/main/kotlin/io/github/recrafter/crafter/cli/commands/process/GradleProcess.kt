package io.github.recrafter.crafter.cli.commands.process

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.recrafter.crafter.cli.bash.ExitCode.SUCCESS
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor.YELLOW
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.conditions.BashConditions.isPidAlive
import io.github.recrafter.crafter.cli.bash.properties.CommandInputReference
import io.github.recrafter.crafter.cli.bash.variables.*
import io.github.recrafter.crafter.cli.extensions.common.Builder
import io.github.recrafter.crafter.cli.extensions.rounded
import io.github.recrafter.crafter.cli.extensions.singleQuoted

open class GradleProcess(
    builder: ScriptBuilder,
    val taskName: String,
    protected val varNamePart: String = taskName.setCase(camelCase, SCREAMING_SNAKE_CASE)
) {
    val pid: StringVar = builder.initString(varNamePart + "_PID")
    val exitCode: IntVar = builder.initInt(varNamePart + "_EXIT_CODE")
    val logWatcher: LogWatcher = LogWatcher.build(builder, varNamePart)

    open val input: CommandInputReference? = null

    open val onPrepare: ScriptBuilder.(spinner: Spinner) -> ScriptBuilder = { this }
    open val onLogWatch: ScriptBuilder.(newLine: StringVar) -> ScriptBuilder = { this }
    open val onSuccess: Builder<ScriptBuilder> = { this }
    open val onError: Builder<ScriptBuilder> = { this }
    open val onCancel: Builder<ScriptBuilder> = {
        print_("Canceling ${INTERRUPT_KEY.rounded()}...", YELLOW)
        ensureProcessKilled(pid)
    }
    open val onInterrupted: Builder<ScriptBuilder> = { this }

    open fun ScriptBuilder.printTaskName(color: AnsiColor): ScriptBuilder {
        print_("Task ")
        print_(taskName.singleQuoted(), color)
        return this
    }

    open fun printLogLine(builder: ScriptBuilder) = builder.run {
        print_(logWatcher.line)
    }

    fun runLogWatcher(builder: ScriptBuilder) = builder.run {
        setStringValue(logWatcher.pid, runFileWatcherInBackground(logWatcher.path, pid) { newLine ->
            onLogWatch(newLine)
            notifyVarChanged(logWatcher.queue, newLine.value)
        })
    }

    fun printStatus(builder: ScriptBuilder, spinner: Spinner) = builder.run {
        clearLine()
        ifBlock {
            if_(bash.conditions.isPidAlive(pid)) {
                onPrepare(spinner)
            }.else_ {
                ifBlock {
                    if_(exitCode.isEmpty()) {
                        setIntValue(exitCode, wait(pid))
                    }
                }
                ifBlock {
                    if_(exitCode.equals_(SUCCESS)) {
                        onSuccess()
                    }.else_ {
                        onError()
                    }
                }
            }
        }
    }

    fun interrupt(builder: ScriptBuilder): ScriptBuilder = builder.run {
        ensureProcessKilled(logWatcher.pid)
        ifBlock {
            if_(bash.conditions.isPidAlive(pid)) {
                clearLine()
                onCancel()
                clearLine()
                onInterrupted()
            }
        }
    }

    companion object {
        const val INTERRUPT_KEY: String = "Ctrl+C"

        fun build(builder: ScriptBuilder, taskName: String): GradleProcess =
            GradleProcess(builder, taskName, taskName)
    }
}
