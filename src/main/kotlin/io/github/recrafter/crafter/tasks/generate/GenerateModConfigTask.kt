package io.github.recrafter.crafter.tasks.generate

import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.serialization.serializeJsonToFile
import io.github.diskria.kotlin.utils.extensions.serialization.serializeToJson
import io.github.diskria.kotlin.utils.extensions.serialization.serializeToToml
import io.github.diskria.kotlin.utils.extensions.serialization.serializeTomlToFile
import io.github.recrafter.bedrock.loaders.ModLoaderType.*
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.core.CrafterConstants
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.fabric.config.FabricModConfig
import io.github.recrafter.crafter.forge.config.ForgeModConfig
import io.github.recrafter.crafter.neoforge.config.NeoForgeModConfig
import io.github.recrafter.crafter.ornithe.config.OrnitheModConfig
import io.github.recrafter.crafter.quilt.config.QuiltModConfig
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
        group = CrafterConstants.TASK_GROUP
    }

    @TaskAction
    fun generate() {
        val mod = mod.get()
        val splitSide = splitSide.orNull
        val outputFile = outputFile.get().asFile.ensureFileExists()

        when (mod.loader) {
            FABRIC -> {
                val config = FabricModConfig.of(mod, splitSide)
                mod.log(project, "Mod config generated", config.serializeToJson())
                config.serializeJsonToFile(outputFile)
            }

            QUILT -> {
                val config = QuiltModConfig.of(mod, splitSide)
                mod.log(project, "Mod config generated", config.serializeToJson())
                config.serializeJsonToFile(outputFile)
            }

            LEGACY_FABRIC -> {
                val config = FabricModConfig.of(mod, splitSide)
                mod.log(project, "Mod config generated", config.serializeToJson())
                config.serializeJsonToFile(outputFile)
            }

            ORNITHE -> {
                val config = OrnitheModConfig.of(mod, splitSide)
                mod.log(project, "Mod config generated", config.serializeToJson())
                config.serializeJsonToFile(outputFile)
            }

            BABRIC -> {
                val config = FabricModConfig.of(mod, splitSide)
                mod.log(project, "Mod config generated", config.serializeToJson())
                config.serializeJsonToFile(outputFile)
            }

            FORGE -> {
                val config = ForgeModConfig.of(mod)
                mod.log(project, "Mod config generated", config.serializeToToml())
                config.serializeTomlToFile(outputFile)
            }

            NEOFORGE -> {
                val config = NeoForgeModConfig.of(mod)
                mod.log(project, "Mod config generated", config.serializeToToml())
                config.serializeTomlToFile(outputFile)
            }
        }
    }
}
