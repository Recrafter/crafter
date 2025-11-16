package io.github.recrafter.crafter.extensions

import io.github.recrafter.crafter.extensions.mappers.toInt
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val TaskContainer.loomRemapJar: TaskProvider<RemapJarTask>
    get() = named<RemapJarTask>("remapJar")

inline fun <reified T : Task> TaskContainer.lazyConfigure(taskName: String, crossinline configure: T.() -> Unit) {
    matching { it.name == taskName && T::class.isInstance(it) }.configureEach { (this as T).configure() }
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
