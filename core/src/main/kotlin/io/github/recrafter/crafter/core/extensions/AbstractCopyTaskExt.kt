package io.github.recrafter.crafter.core.extensions

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.diskria.kotlin.utils.extensions.toNullIfEmpty
import org.gradle.api.Task
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.TaskProvider
import java.io.File

fun AbstractCopyTask.copyTaskOutput(taskProvider: TaskProvider<out Task>, destinationPath: String? = null) {
    dependsOn(taskProvider)
    from(taskProvider) {
        if (destinationPath?.contains(Constants.Char.SLASH) == true) {
            destinationPath.toNullIfEmpty()?.let { into(it.substringBeforeLast(Constants.Char.SLASH)) }
        }
    }
}

fun AbstractCopyTask.copyFile(file: File, destinationPath: String? = null) {
    from(file) {
        if (destinationPath?.contains(Constants.Char.SLASH) == true) {
            destinationPath.toNullIfEmpty()?.let { into(it.substringBeforeLast(Constants.Char.SLASH)) }
        }
    }
}

fun AbstractCopyTask.moveFile(source: String, target: String) {
    rename(source, target)
}

fun AbstractCopyTask.excludeRecursively(directoryPath: String) {
    exclude(directoryPath.appendPath(Constants.Char.ASTERISK.repeat(2)))
}
