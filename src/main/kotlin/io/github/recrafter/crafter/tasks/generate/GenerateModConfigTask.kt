package io.github.recrafter.crafter.tasks.generate

import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.serialization.serializeJsonToFile
import io.github.diskria.kotlin.utils.extensions.serialization.serializeTomlToFile
import io.github.recrafter.bedrock.loaders.ModLoaderType.*
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.CrafterGradlePlugin
import io.github.recrafter.crafter.configs.fabric.FabricModConfig
import io.github.recrafter.crafter.configs.forge.ForgeModConfig
import io.github.recrafter.crafter.configs.neoforge.NeoForgeModConfig
import io.github.recrafter.crafter.configs.ornithe.OrnitheModConfig
import io.github.recrafter.crafter.configs.quilt.QuiltModConfig
import io.github.recrafter.crafter.models.Mod
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class GenerateModConfigTask : DefaultTask() {

    @get:Internal
    abstract val mod: Property<Mod>

    @get:Internal
    abstract val splitSide: Property<ModSide>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        group = CrafterGradlePlugin.PLUGIN_NAME
    }

    @TaskAction
    fun generate() {
        val minecraftMod = mod.get()
        val splitSide = splitSide.orNull
        val outputFile = outputFile.get().asFile.ensureFileExists()

        when (minecraftMod.loader) {
            FABRIC -> FabricModConfig.of(minecraftMod, splitSide).serializeJsonToFile(outputFile)
            QUILT -> QuiltModConfig.of(minecraftMod, splitSide).serializeJsonToFile(outputFile)
            LEGACY_FABRIC -> FabricModConfig.of(minecraftMod, splitSide).serializeJsonToFile(outputFile)
            ORNITHE -> OrnitheModConfig.of(minecraftMod, splitSide).serializeJsonToFile(outputFile)
            BABRIC -> FabricModConfig.of(minecraftMod, splitSide).serializeJsonToFile(outputFile)
            FORGE -> ForgeModConfig.of(minecraftMod).serializeTomlToFile(outputFile)
            NEOFORGE -> NeoForgeModConfig.of(minecraftMod).serializeTomlToFile(outputFile)
        }
    }
}
