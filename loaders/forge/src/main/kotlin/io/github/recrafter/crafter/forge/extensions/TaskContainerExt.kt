package io.github.recrafter.crafter.forge.extensions

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

inline fun <reified T : Task> TaskContainer.lazyConfigure(taskName: String, crossinline configure: T.() -> Unit) {
    matching { it.name == taskName && T::class.isInstance(it) }.configureEach { (this as T).configure() }
}
