package io.github.recrafter.crafter.fabric

import io.github.diskria.gradle.utils.extensions.common.buildArtifactCoordinates
import io.github.diskria.gradle.utils.extensions.projectDirectory
import io.github.diskria.gradle.utils.extensions.restoreDependencyResolutionRepositories
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
import io.github.recrafter.bedrock.versions.isIntegratedServer
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.ModLoaderAdapter
import io.github.recrafter.crafter.core.extensions.*
import io.github.recrafter.crafter.core.helpers.AccessConfigHelper
import io.github.recrafter.crafter.fabric.extensions.quilt
import io.ktor.http.*
import net.fabricmc.loom.util.Constants.TaskGroup
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import java.io.File

abstract class FabricFamilyAdapter(val loader: ModLoaderType) : ModLoaderAdapter() {

    open val extensionPluginPackageName: String? = null
    open val customVersionManifest: Url? = null

    open fun configureExtensionPlugin(project: Project) {
    }

    open fun getLoaderDependency(mod: Mod): String =
        buildArtifactCoordinates("net.fabricmc", "fabric-loader", mod.loaderMetadata.loaderVersion)

    open fun getCustomMinecraftMetadataUrl(minecraftVersion: MinecraftVersion): Url? = null

    open fun getCustomIntermediaryUrl(placeholder: String = "%1\$s"): String? = null

    abstract fun getMappingsDependency(project: Project, mod: Mod): Any

    override fun getAccessConfigPreset(): String = AccessConfigHelper.WIDENER_PRESET

    override fun configurePlugin(
        mod: Mod,
        project: Project,
        runDirectory: File,
        accessConfig: File,
        isRunConfigurationsDisabled: Boolean,
    ) = with(project) {
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
            accessWidenerPath = accessConfig
            runs {
                ModSide.values().forEach { side ->
                    named(side.getName()) {
                        ideConfigGenerated(!isRunConfigurationsDisabled)
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
            @Suppress("UnstableApiUsage")
            mixin {
                useLegacyMixinAp = true
                defaultRefmapName = mod.refmapFileName
            }
        }
        tasks {
            if (isRunConfigurationsDisabled) {
                lazyDisable("ideaSyncTask")
            }
            if (!mod.minecraftVersion.isIntegratedServer) {
                lazyDisable("extractNatives")
            }
        }
        restoreDependencyResolutionRepositories()
        dependencies {
            val minecraftDependency = buildArtifactCoordinates(
                "com.mojang",
                "minecraft",
                mod.minecraftVersion.asString()
            )
            mod.log(project, "Minecraft: $minecraftDependency")
            minecraft(minecraftDependency)

            val loaderDependency = getLoaderDependency(mod)
            mod.log(project, "Loader: $loaderDependency")
            modImplementation(loaderDependency)

            val mappingsDependency = getMappingsDependency(project, mod)
            mod.log(project, "Mappings: $mappingsDependency")
            mappings(mappingsDependency)
        }
        groupLoaderTasks(
            loaderPackageNamePrefixes = listOfNotNull("net.fabricmc.loom", extensionPluginPackageName),
            taskGroups = listOf(TaskGroup.FABRIC, TaskGroup.IDE),
            loader = loader,
        )
    }
}
