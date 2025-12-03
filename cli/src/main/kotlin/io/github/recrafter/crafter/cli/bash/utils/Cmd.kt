package io.github.recrafter.crafter.cli.bash.utils

import io.github.diskria.gradle.utils.extensions.common.gradleTaskPath
import io.github.diskria.gradle.utils.helpers.jvm.JvmArguments
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.diskria.kotlin.utils.extensions.generics.toNullIfEmpty

object Cmd {

    fun of(scriptName: String, arguments: String): String =
        buildString {
            append(Constants.Char.DOT)
            append(Constants.Char.SLASH)
            append(scriptName)
            append(Constants.Char.SPACE)
            append(arguments)
        }

    fun gradleTask(
        taskName: String,
        projectPath: String? = null,
        arguments: Map<String, String>? = null,
        vararg flags: String,
    ): String =
        of("gradlew", buildString {
            if (projectPath != null) {
                append(gradleTaskPath(taskName, projectPath))
            } else {
                append(taskName)
            }
            arguments?.toNullIfEmpty()?.let { arguments ->
                append(Constants.Char.SPACE)
                append(arguments.toList().joinBySpace { (name, value) ->
                    JvmArguments.property(name, value)
                })
            }
            val flagsList = flags.toList()
            flagsList.toNullIfEmpty()?.let {
                append(Constants.Char.SPACE)
                append(flagsList.joinBySpace())
            }
        })
}
