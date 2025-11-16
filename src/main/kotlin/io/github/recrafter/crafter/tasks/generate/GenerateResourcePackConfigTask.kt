package io.github.recrafter.crafter.tasks.generate

import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.serialization.serializeJsonToFile
import io.github.recrafter.crafter.CrafterGradlePlugin
import io.github.recrafter.crafter.configs.packs.resources.ResourcePackConfig
import io.github.recrafter.crafter.models.Mod
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
        group = CrafterGradlePlugin.PLUGIN_NAME
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
