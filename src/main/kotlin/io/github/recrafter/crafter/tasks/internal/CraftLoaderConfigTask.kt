package io.github.recrafter.crafter.tasks.internal

import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.serialization.serializeToJson
import io.github.diskria.kotlin.utils.extensions.serialization.serializeToToml
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.crafter.core.CrafterTasks
import io.github.recrafter.crafter.core.properties.ModProperty
import io.github.recrafter.crafter.loaders.fabric.config.FabricModConfig
import io.github.recrafter.crafter.loaders.forge.config.ForgeModConfig
import io.github.recrafter.crafter.loaders.neoforge.config.NeoForgeModConfig
import io.github.recrafter.crafter.loaders.ornithe.config.OrnitheModConfig
import io.github.recrafter.crafter.loaders.quilt.config.QuiltModConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*

abstract class CraftLoaderConfigTask : DefaultTask() {

    @get:Nested
    abstract val mod: ModProperty

    @get:Internal
    abstract val accessorConfig: RegularFileProperty

    @get:Input
    val isAccessorExists: Boolean
        get() = accessorConfig.isPresent && accessorConfig.get().asFile.isFile

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        group = CrafterTasks.INTERNAL_GROUP
    }

    @TaskAction
    fun generate() {
        val mod = mod.get()
        val outputFile = outputFile.get().asFile.ensureFileExists()

        val config = when (mod.loader) {
            ModLoaderType.FABRIC -> FabricModConfig.of(mod, isAccessorExists).serializeToJson()
            ModLoaderType.QUILT -> QuiltModConfig.of(mod, isAccessorExists).serializeToJson()
            ModLoaderType.LEGACY_FABRIC -> FabricModConfig.of(mod, isAccessorExists).serializeToJson()
            ModLoaderType.BABRIC -> FabricModConfig.of(mod, isAccessorExists).serializeToJson()
            ModLoaderType.ORNITHE -> OrnitheModConfig.of(mod, isAccessorExists).serializeToJson()
            ModLoaderType.FORGE -> ForgeModConfig.of(mod).serializeToToml()
            ModLoaderType.NEOFORGE -> NeoForgeModConfig.of(mod, isAccessorExists).serializeToToml()
        }
        mod.log(project, "Loader config generated", config)
        outputFile.writeText(config)
    }
}
