package io.github.recrafter.crafter.core.extensions

import io.github.diskria.gradle.utils.extensions.disable
import io.github.recrafter.crafter.core.CrafterConstants
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

fun Task.isCrafterTask(): Boolean =
    group == CrafterConstants.TASKS_CATEGORY ||
            group == CrafterConstants.INTERNAL_TASKS_CATEGORY

inline fun <reified T : Task> TaskContainer.lazyDisableTyped(taskName: String) {
    matching { it.name == taskName && T::class.isInstance(it) }.configureEach { (this as T).disable() }
}

fun TaskContainer.lazyDisable(taskName: String) {
    lazyDisableTyped<Task>(taskName)
}
