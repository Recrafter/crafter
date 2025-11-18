package io.github.recrafter.crafter.core

import io.github.diskria.kotlin.utils.Constants
import io.github.recrafter.bedrock.sides.ModSide
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

interface ModLoader {
    fun configurePlugin(mod: Mod, project: Project, sides: Set<ModSide>, accessConfig: File): Any?
    fun getPrepareRunTasks(pluginProject: Project, side: ModSide): List<Task> = emptyList()
    fun getAccessConfigPreset(): String = Constants.Char.EMPTY
    fun isDataPackConfigRequired(): Boolean = false
}
