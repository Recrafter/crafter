package io.github.recrafter.crafter.loaders.fabric

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import io.github.diskria.gradle.utils.extensions.*
import io.github.diskria.gradle.utils.extensions.common.artifact
import io.github.diskria.gradle.utils.helpers.jvm.JvmArguments
import io.github.diskria.gradle.utils.helpers.jvm.Size
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.`Title Case`
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.era.common.MinecraftEra
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.ModLoaderAdapter
import io.github.recrafter.crafter.core.extensions.*
import io.github.recrafter.crafter.loaders.fabric.extensions.quilt
import io.github.recrafter.crafter.mixins.Lapis
import io.ktor.http.*
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.ValidateAccessWidenerTask
import net.fabricmc.loom.util.Constants.TaskGroup
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.repositories
import java.io.File

abstract class FabricFamilyAdapter(val loader: ModLoaderType) : ModLoaderAdapter() {

    open val extensionPluginPackageName: String? = null
    open val customVersionManifest: Url? = null

    open fun configureExtensionPlugin(project: Project) {
    }

    open fun getLoaderDependency(mod: Mod): String =
        artifact("net.fabricmc", "fabric-loader", mod.loaderMetadata.loaderVersion)

    open fun getCustomMinecraftMetadataUrl(minecraftVersion: MinecraftVersion): Url? = null

    open fun getCustomIntermediaryUrl(placeholder: String = $$"%1$s"): String? = null

    open fun getMappingsDependency(project: Project, mod: Mod): Any = with(project) {
        @Suppress("UnstableApiUsage")
        quilt.layered { officialMojangMappings() }
    }

    override fun getGameJars(project: Project): FileCollection =
        project.quilt.namedMinecraftJars

    override fun configurePlugin(mod: Mod, project: Project, runDirectory: File, accessorConfig: File) = with(project) {
        quilt {
            configureExtensionPlugin(project)
            getCustomMinecraftMetadataUrl(mod.minecraftVersion)?.let {
                customMinecraftMetadata = it.toString()
            }
            getCustomIntermediaryUrl()?.let {
                intermediaryUrl = it
            }
            customVersionManifest?.let {
                @Suppress("UnstableApiUsage")
                versionsManifests.add(loader.displayName, it.toString())
            }
            if (accessorConfig.isFile) {
                accessWidenerPath = accessorConfig
            }
            runs {
                ModSide.entries.forEach { side ->
                    named(side.getName()) {
                        ideConfigGenerated(false)
                        name = side.getName(`Title Case`)
                        runDir = runDirectory.resolve(side.getName()).relativeTo(projectDirectory).path
                        when (side) {
                            ModSide.CLIENT -> client()
                            ModSide.SERVER -> server()
                        }
                        val memoryRange = when (side) {
                            ModSide.CLIENT -> 2..4
                            ModSide.SERVER -> 4..8
                        }
                        vmArgs(
                            *JvmArguments.memory(memoryRange, Size.GIGABYTES),
                            JvmArguments.property("mixin.debug.export", true),
                        )
                        if (mod.minecraftVersion.era < MinecraftEra.ALPHA) {
                            vmArgs(
                                JvmArguments.property("fabric.gameVersion", mod.minecraftVersion.asString()),
                            )
                        }
                        if (side == ModSide.CLIENT) {
                            programArgs(
                                *JvmArguments.program("username", mod.player),
                                *JvmArguments.program("userProperties", Constants.Json.EMPTY_OBJECT),
                            )
                        }
                    }
                }
            }
        }
        tasks {
            lazyDisable("ideaSyncTask")
            lazyConfigure<ValidateAccessWidenerTask>("validateAccessWidener") {
                dependsOn("kspKotlin")
            }
        }
        restoreDependencyResolutionRepositories()
        val hasKotlinMod = loader == ModLoaderType.FABRIC || loader == ModLoaderType.QUILT
        dependencies {
            val minecraftDependency = artifact("com.mojang", "minecraft", mod.minecraftVersion.asString())
            mod.log(project, "Minecraft: $minecraftDependency")
            minecraft(minecraftDependency)

            val loaderDependency = getLoaderDependency(mod)
            mod.log(project, "Loader: $loaderDependency")
            modImplementation(loaderDependency)

            if (hasKotlinMod) {
                val kotlinDependency = artifact("net.fabricmc", "fabric-language-kotlin", "1.13.7+kotlin.2.2.21")
                mod.log(project, "Kotlin: $loaderDependency")
                modImplementation(kotlinDependency)
            }

            val mappingsDependency = getMappingsDependency(project, mod)
            mod.log(project, "Mappings: $mappingsDependency")
            mappings(mappingsDependency)

            repositories {
                mavenLocal()
            }

            compileOnly(Lapis.GROUP_ID, Lapis.ANNOTATIONS_ARTIFACT_ID, Lapis.VERSION)
            val lapisDependency = artifact(Lapis.GROUP_ID, Lapis.KSP_ARTIFACT_ID, Lapis.VERSION)
            mod.log(project, "Lapis: $lapisDependency")
            ksp(lapisDependency)
        }
        if (!hasKotlinMod) {
            shadowKotlin(mod.packageName)
            tasks {
                shadowJar {
                    destinationDirectory = getBuildDirectory("devlibs")
                    archiveClassifier = "shadow-dev"
                }
                lazyConfigure<RemapJarTask>("remapJar") {
                    inputFile = shadowJar.flatMap { it.archiveFile }
                    dependsOn(shadowJar)
                }
            }
        }
        groupLoaderTasks(
            loaderPackageNamePrefixes = listOfNotNull("net.fabricmc.loom", extensionPluginPackageName),
            taskGroups = listOf(TaskGroup.FABRIC, TaskGroup.IDE),
            loader = loader,
        )
    }
}
