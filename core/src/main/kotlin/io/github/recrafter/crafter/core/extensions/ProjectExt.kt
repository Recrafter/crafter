package io.github.recrafter.crafter.core.extensions

import io.github.recrafter.bedrock.loaders.ModLoaderType
import org.gradle.api.Project

fun Project.groupLoaderTasks(
    loaderPackageName: List<String> = emptyList(),
    taskGroups: List<String> = emptyList(),
    taskNames: List<String> = emptyList(),
    loader: ModLoaderType,
) {
    gradle.taskGraph.whenReady {
        tasks.matching { task ->
            val packageName = task.javaClass.packageName.orEmpty()
            loaderPackageName.any { packageName.startsWith(it) } ||
                    task.group != null && taskGroups.contains(task.group) ||
                    taskNames.contains(task.name)
        }.configureEach {
            group = "Mod Loader/${loader.displayName}"
        }
    }
}

fun Project.groupMatchingTasks(name: String, vararg keywords: String) {
    gradle.taskGraph.whenReady {
        tasks
            .matching { task ->
                !task.isCrafterTask() && keywords.any { task.name.lowercase().contains(it.lowercase()) }
            }
            .configureEach { group = name }
    }
}
