package io.github.recrafter.crafter.tasks.generate

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.*
import io.github.diskria.kotlin.utils.extensions.common.`dot․case`
import io.github.diskria.kotlin.utils.extensions.common.`path∕case`
import io.github.diskria.kotlin.utils.extensions.serialization.serializeJsonToFile
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.CrafterGradlePlugin
import io.github.recrafter.crafter.configs.mixins.MixinsConfig
import io.github.recrafter.crafter.helpers.MixinsHelper
import io.github.recrafter.crafter.models.Mod
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateModMixinsConfigTask : DefaultTask() {

    @get:Internal
    abstract val mod: Property<Mod>

    @get:Internal
    abstract val sideSourceSetDirectories: MapProperty<ModSide, File>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        group = CrafterGradlePlugin.PLUGIN_NAME
    }

    @TaskAction
    fun generate() {
        val mod = mod.get()
        val sideSourceSetDirectories = sideSourceSetDirectories.get()
        val outputFile = outputFile.get().asFile

        val sideMixins = sideSourceSetDirectories.mapValues {
            val mixinsRoot = it.value.resolve(mod.packagePath).resolve(MixinsHelper.MIXINS_NAME)
            mixinsRoot
                .walkDirectories()
                .flatMap { directory ->
                    val relativePath = directory.relativeTo(mixinsRoot).path
                    directory.listFilesWithExtension(Constants.File.Extension.JAVA).map { javaFile ->
                        val className = javaFile.nameWithoutExtension
                        if (relativePath.isEmpty()) className
                        else relativePath.setCase(`path∕case`, `dot․case`).appendPackageName(className)
                    }
                }
                .toList()
                .sorted()
        }.filterValues { it.isNotEmpty() }

        val config = MixinsConfig.of(mod, sideMixins)
        config.serializeJsonToFile(outputFile.ensureFileExists())
    }
}
