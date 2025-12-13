package io.github.recrafter.crafter.cli.commands.process

import io.github.diskria.gradle.utils.extensions.common.requireGradleNotNull
import io.github.diskria.gradle.utils.extensions.taskName
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.MinecraftConstants
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor.*
import io.github.recrafter.crafter.cli.bash.ansi.AnsiStyle.BOLD
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.builder.initVarSync
import io.github.recrafter.crafter.cli.bash.properties.CommandInputReference
import io.github.recrafter.crafter.cli.bash.properties.stringVar
import io.github.recrafter.crafter.cli.bash.variables.*
import io.github.recrafter.crafter.cli.extensions.angled
import io.github.recrafter.crafter.cli.extensions.rounded
import io.github.recrafter.crafter.cli.extensions.squared
import io.github.recrafter.crafter.core.helpers.server.ServerCommands
import io.github.recrafter.crafter.tasks.CraftClientTask
import io.github.recrafter.crafter.tasks.CraftServerTask

class CraftProcess(
    builder: ScriptBuilder,
    val side: ModSide,
    val isLauncher: Boolean,
) : GradleProcess(
    builder = builder,
    varNamePart = side.name,
    taskName = when (side) {
        ModSide.CLIENT -> CraftClientTask::class.taskName
        ModSide.SERVER -> CraftServerTask::class.taskName
    },
) {
    val isRunning: BooleanVar = builder.initBoolean("IS_${varNamePart}_RUNNING")
    val runningStateSync: VarSync = builder.initVarSync(isRunning)

    override val input: CommandInputReference? = when (side) {
        ModSide.CLIENT -> null
        ModSide.SERVER -> builder.initCommandInput("SERVER_INPUT")
    }

    override val onPrepare: ScriptBuilder.(spinner: Spinner) -> ScriptBuilder = { spinner ->
        ifBlock {
            if_(isRunning.equals_(true)) {
                printTaskName(GREEN)
                print_(" is running...", GREEN)
                print_(" (Press ", GRAY)
                print_(INTERRUPT_KEY.angled(), GRAY, BOLD)
                print_(" to stop)", GRAY)
            }.else_ {
                val spinnerChar by stringVar(spinner.chars.getCharAt(spinner.progress.mod(spinner.length)))
                print_(spinnerChar, CYAN)
                print_(" Crafting ", CYAN)
                printTaskName(CYAN)
                print_("...", CYAN)
                incrementIntValue(spinner.progress)
            }
        }
    }

    override val onLogWatch: ScriptBuilder.(newLine: StringVar) -> ScriptBuilder = { newLine ->
        ifBlock {
            if_(isRunning.equals_(false)) {
                ifBlock {
                    ifAny(side.logRunningIndicators.map { newLine.contains(it) }) {
                        setBooleanValue(isRunning, true)
                        notifyVarChanged(runningStateSync)
                    }
                }
            }
        }
    }

    override val onSuccess: ScriptBuilder.() -> ScriptBuilder = script@{
        printTaskName(GRAY)
        print_(" closed.", GRAY)
        if (!isLauncher) {
            println_()
        }
        return@script this
    }

    override val onError: ScriptBuilder.() -> ScriptBuilder = script@{
        printTaskName(RED)
        print_(" crashed. ", RED)
        print_("See full log at ${bash.getAbsolutePath(logWatcher.path.value)}", GRAY)
        if (!isLauncher) {
            println_()
        }
        return@script this
    }

    override val onCancel: ScriptBuilder.() -> ScriptBuilder = script@{
        print_("Canceling ${INTERRUPT_KEY.rounded()}...", YELLOW)
        when (side) {
            ModSide.SERVER -> {
                sendCommand(
                    requireGradleNotNull(input) { "Server process must be have input for stop" },
                    ServerCommands.STOP
                )
                wait(pid)
            }

            else -> ensureProcessKilled(pid)
        }
        return@script this
    }

    override val onInterrupted: ScriptBuilder.() -> ScriptBuilder = script@{
        ifBlock {
            if_(isRunning.equals_(false)) {
                print_("Crafting ", RED)
                printTaskName(RED)
                print_(" was interrupted by user.", RED)
                if (isLauncher) {
                    println_()
                }
                return@if_ this
            }.else_ {
                print_("Stopping ", GRAY)
                printTaskName(GRAY)
                print_(" ${INTERRUPT_KEY.rounded()}...", GRAY)
                if (isLauncher && side == ModSide.CLIENT) {
                    println_()
                }
                return@else_ this
            }
        }
        if (!isLauncher) {
            println_()
        }
        return@script this
    }

    private val ModSide.logTagColor: AnsiColor
        get() = when (this) {
            ModSide.CLIENT -> MAGENTA
            ModSide.SERVER -> YELLOW
        }

    private val ModSide.logRunningIndicators: List<String>
        get() = when (this) {
            ModSide.CLIENT -> listOf("[Render thread/", "LWJGL", "fps,")
            ModSide.SERVER -> listOf("Done")
        }

    override fun printLogLine(builder: ScriptBuilder) = builder.run {
        if (isLauncher) {
            print_(side.name.squared(), side.logTagColor)
            print_(spaces(1))
        }
        super.printLogLine(builder)
    }

    override fun ScriptBuilder.printTaskName(color: AnsiColor): ScriptBuilder {
        print_(MinecraftConstants.FULL_GAME_NAME, color)
        print_(" ${side.getName()}", color, BOLD)
        return this
    }

    companion object {
        fun build(builder: ScriptBuilder, side: ModSide, isLauncher: Boolean = false): CraftProcess =
            CraftProcess(builder, side, isLauncher)
    }
}
