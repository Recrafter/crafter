package io.github.recrafter.crafter.core.extensions

import io.github.diskria.gradle.utils.extensions.disable
import io.github.recrafter.crafter.core.CrafterConstants
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Task.isCrafterTask(): Boolean =
    group == CrafterConstants.PUBLIC_TASKS_GROUP || group == CrafterConstants.INTERNAL_TASKS_GROUP

inline fun <reified T : Task> TaskContainer.lazyDisableTyped(taskName: String) {
    matching { it.name == taskName && T::class.isInstance(it) }.configureEach { (this as T).disable() }
}

fun TaskContainer.lazyDisable(taskName: String) {
    lazyDisableTyped<Task>(taskName)
}

@Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")
fun TaskContainer.configureJvmTarget(target: JvmTarget) {
    withType<JavaCompile>().configureEach {
        options.release = target.toInt()
    }
    withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget = target
    }
}

inline fun <reified T : Task> TaskContainer.lazyConfigure(taskName: String, crossinline configure: T.() -> Unit) {
    matching { it.name == taskName && T::class.isInstance(it) }.configureEach { (this as T).configure() }
}
