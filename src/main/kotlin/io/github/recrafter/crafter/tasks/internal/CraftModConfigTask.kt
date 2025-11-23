package io.github.recrafter.crafter.tasks.internal

import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.serialization.serializeToJson
import io.github.diskria.kotlin.utils.extensions.serialization.serializeToToml
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.crafter.core.CrafterConstants
import io.github.recrafter.crafter.core.properties.ModProperty
import io.github.recrafter.crafter.fabric.config.FabricModConfig
import io.github.recrafter.crafter.forge.config.ForgeModConfig
import io.github.recrafter.crafter.neoforge.config.NeoForgeModConfig
import io.github.recrafter.crafter.ornithe.config.OrnitheModConfig
import io.github.recrafter.crafter.quilt.config.QuiltModConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class CraftModConfigTask : DefaultTask() {

    @get:Nested
    abstract val mod: ModProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        group = CrafterConstants.INTERNAL_TASKS_GROUP
    }

    @TaskAction
    fun generate() {
        val mod = mod.get()
        val outputFile = outputFile.get().asFile.ensureFileExists()

        when (mod.loader) {
            ModLoaderType.FABRIC -> {
                val config = FabricModConfig.of(mod).serializeToJson()
                mod.log(project, "Mod config generated", config)
                outputFile.writeText(config)
            }

            ModLoaderType.QUILT -> {
                val config = QuiltModConfig.of(mod).serializeToJson()
                mod.log(project, "Mod config generated", config)
                outputFile.writeText(config)
            }

            ModLoaderType.LEGACY_FABRIC -> {
                val config = FabricModConfig.of(mod).serializeToJson()
                mod.log(project, "Mod config generated", config)
                outputFile.writeText(config)
            }

            ModLoaderType.BABRIC -> {
                val config = FabricModConfig.of(mod).serializeToJson()
                mod.log(project, "Mod config generated", config)
                outputFile.writeText(config)
            }

            ModLoaderType.ORNITHE -> {
                val config = OrnitheModConfig.of(mod).serializeToJson()
                mod.log(project, "Mod config generated", config)
                outputFile.writeText(config)
            }

            ModLoaderType.FORGE -> {
                val config = ForgeModConfig.of(mod).serializeToToml()
                mod.log(project, "Mod config generated", config)
                outputFile.writeText(config)
            }

            ModLoaderType.NEOFORGE -> {
                val config = NeoForgeModConfig.of(mod).serializeToToml()
                mod.log(project, "Mod config generated", config)
                outputFile.writeText(config)
            }
        }
    }
}