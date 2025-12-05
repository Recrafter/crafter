package io.github.recrafter.crafter.cli.commands.process

import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.builder.initVarSync
import io.github.recrafter.crafter.cli.bash.variables.StringVar
import io.github.recrafter.crafter.cli.bash.variables.VarSync
import io.github.recrafter.crafter.cli.bash.variables.VarSyncStrategy

class LogWatcher(val pid: StringVar, val path: StringVar, val line: StringVar, val queue: VarSync) {
    companion object {
        fun build(builder: ScriptBuilder, varNamePart: String): LogWatcher {
            val line = builder.initString("LAST_" + varNamePart + "_LOG_LINE")
            return LogWatcher(
                path = builder.initString(varNamePart + "_LOG_PATH"),
                line = line,
                pid = builder.initString(varNamePart + "_LOG_WATCHER_PID"),
                queue = builder.initVarSync(line, VarSyncStrategy.QUEUE)
            )
        }
    }
}
