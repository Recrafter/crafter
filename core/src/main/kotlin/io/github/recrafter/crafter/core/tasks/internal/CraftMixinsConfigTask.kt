package io.github.recrafter.crafter.core.tasks.internal

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPackageName
import io.github.diskria.kotlin.utils.extensions.common.`dot․case`
import io.github.diskria.kotlin.utils.extensions.common.`path∕case`
import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.serialization.serializeJsonToFile
import io.github.diskria.kotlin.utils.extensions.serialization.serializeToJson
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.core.CrafterTasks
import io.github.recrafter.crafter.core.configs.mixins.MixinsConfig
import io.github.recrafter.crafter.core.extensions.includeFilesWithExtension
import io.github.recrafter.crafter.core.helpers.MixinsHelper
import io.github.recrafter.crafter.core.properties.MixinCollectionProperty
import io.github.recrafter.crafter.core.properties.ModProperty
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import java.io.File

abstract class CraftMixinsConfigTask : DefaultTask() {

    @get:Nested
    abstract val mod: ModProperty

    @get:Internal
    abstract val sourceSetDirectories: MapProperty<ModSide, File>

    @get:Nested
    abstract val mixinCollections: MapProperty<ModSide, MixinCollectionProperty>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        group = CrafterTasks.INTERNAL_TASKS_GROUP
    }

    fun configureInputFiles() {
        val sourceSetDirectories = sourceSetDirectories.get()

        mixinCollections = sourceSetDirectories.mapValues { (_, sourceSetDirectory) ->
            MixinCollectionProperty(
                project.objects.fileCollection().from(project.fileTree(sourceSetDirectory) {
                    includeFilesWithExtension(Constants.File.Extension.JAVA)
                })
            )
        }
    }

    @TaskAction
    fun generate() {
        val mod = mod.get()
        val sourceSetDirectories = sourceSetDirectories.get()
        val mixinCollections = mixinCollections.get()
        val outputFile = outputFile.get().asFile

        val sideMixins = mixinCollections.mapValues { (side, mixinCollection) ->
            val mixinsRoot = sourceSetDirectories
                .getValue(side)
                .resolve(mod.packagePath)
                .resolve(MixinsHelper.MIXINS_NAME)

            mixinCollection.files.map { javaFile ->
                val className = javaFile.nameWithoutExtension
                val directory = javaFile.parentFile
                if (directory == mixinsRoot) {
                    className
                } else {
                    directory
                        .relativeTo(mixinsRoot).path
                        .setCase(`path∕case`, `dot․case`)
                        .appendPackageName(className)
                }
            }.sorted()
        }.filterValues { it.isNotEmpty() }

        val config = MixinsConfig.of(mod, sideMixins)
        mod.log(project, "Mixins config generated", config.serializeToJson())
        config.serializeJsonToFile(outputFile.ensureFileExists())
    }
}
