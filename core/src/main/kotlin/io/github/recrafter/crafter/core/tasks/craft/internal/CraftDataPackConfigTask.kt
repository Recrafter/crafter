package io.github.recrafter.crafter.core.tasks.craft.internal

import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.serialization.serializeJsonToFile
import io.github.diskria.kotlin.utils.extensions.serialization.serializeToJson
import io.github.recrafter.crafter.core.CrafterConstants
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.configs.packs.data.DataPackConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class CraftDataPackConfigTask : DefaultTask() {

    @get:Internal
    abstract val mod: Property<Mod>

    @get:Input
    abstract val minFormat: Property<String>

    @get:Input
    abstract val maxFormat: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        group = CrafterConstants.INTERNAL_TASKS_CATEGORY
    }

    @TaskAction
    fun generate() {
        val mod = mod.get()
        val minFormat = minFormat.get()
        val maxFormat = maxFormat.orNull ?: minFormat
        val outputFile = outputFile.get().asFile

        val config = DataPackConfig.of(mod, minFormat, maxFormat)
        mod.log(project, "Datapack config generated", config.serializeToJson())
        config.serializeJsonToFile(outputFile.ensureFileExists())
    }
}
