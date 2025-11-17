package io.github.recrafter.crafter.core.tasks

import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.serialization.serializeJsonToFile
import io.github.recrafter.crafter.core.CrafterConstants
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.configs.packs.resources.ResourcePackConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class GenerateResourcePackConfigTask : DefaultTask() {

    @get:Internal
    abstract val mod: Property<Mod>

    @get:Input
    abstract val format: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        group = CrafterConstants.TASK_GROUP
    }

    @TaskAction
    fun generate() {
        val mod = mod.get()
        val format = format.get()
        val outputFile = outputFile.get().asFile

        val config = ResourcePackConfig.of(mod, format)
        config.serializeJsonToFile(outputFile.ensureFileExists())
    }
}
