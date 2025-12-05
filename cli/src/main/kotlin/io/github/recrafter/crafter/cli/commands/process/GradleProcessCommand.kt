package io.github.recrafter.crafter.cli.commands.process

import io.github.diskria.gradle.utils.extensions.common.gradleProjectPath
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.KotlinSerializer
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.diskria.kotlin.utils.extensions.generics.joinToString
import io.github.recrafter.bedrock.crafter.CrafterFlow
import io.github.recrafter.crafter.cli.bash.api.commands.AbstractCLICommand
import io.github.recrafter.crafter.cli.bash.api.commands.arguments.common.CLIArguments
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.properties.stringVar
import io.github.recrafter.crafter.cli.bash.utils.Cmd
import io.github.recrafter.crafter.cli.bash.variables.StringVar
import io.github.recrafter.crafter.cli.bash.variables.value
import io.github.recrafter.crafter.tasks.public.InstallCrafterCLITask

abstract class GradleProcessCommand<T : CLIArguments>(
    serializer: KotlinSerializer<T>
) : AbstractCLICommand<T>(serializer) {

    protected fun ScriptBuilder.runTask(
        loader: StringVar, version: StringVar, modProjectName: StringVar, process: GradleProcess,
    ) {
        val command = Cmd.gradleTask(
            process.taskName,
            gradleProjectPath(loader.value, modProjectName.value),
            mapOf(
                "crafter.flow" to CrafterFlow.Single.name,
                "crafter.loader" to loader.value,
                "crafter.version" to version.value,
                "crafter.modProjectName" to modProjectName.value,
            ),
            "--no-daemon", "--stacktrace",
        )
        val logsDirectoryPath by stringVar(InstallCrafterCLITask.CLI_CACHE_DIRECTORY_PATH.appendPath("gradle-output"))
        createDirectory(logsDirectoryPath.value)
        val logName = listOf(process.taskName, loader, version, bash.nowDate()).joinToString(Constants.Char.UNDERSCORE)
        setStringValue(process.logWatcher.path, logsDirectoryPath.toString().appendPath(fileName(logName, "log")))
        setStringValue(process.pid, runCommandInBackground(command, process.logWatcher.path, process.input))
    }
}
