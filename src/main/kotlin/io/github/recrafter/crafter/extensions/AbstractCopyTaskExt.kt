package io.github.recrafter.crafter.extensions

import org.gradle.api.Task
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.TaskProvider
import java.io.File

fun AbstractCopyTask.copyTaskOutput(taskProvider: TaskProvider<out Task>, destinationPath: String? = null) {
    dependsOn(taskProvider)
    from(taskProvider) {
        destinationPath?.let { into(it) }
    }
}

fun AbstractCopyTask.copyFile(file: File, destinationPath: String? = null) {
    from(file) {
        destinationPath?.let { into(it) }
    }
}

fun AbstractCopyTask.moveFile(source: String, target: String) {
    rename(source, target)
}
