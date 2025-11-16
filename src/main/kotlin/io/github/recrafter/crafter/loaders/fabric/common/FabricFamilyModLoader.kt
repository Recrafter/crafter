package io.github.recrafter.crafter.loaders.fabric.common

import io.github.diskria.gradle.utils.extensions.common.buildArtifactCoordinates
import io.github.diskria.gradle.utils.extensions.projectDirectory
import io.github.diskria.gradle.utils.extensions.restoreDependencyResolutionRepositories
import io.github.diskria.gradle.utils.helpers.jvm.JvmArguments
import io.github.diskria.gradle.utils.helpers.jvm.Size
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.`Title Case`
import io.github.diskria.kotlin.utils.extensions.common.failWithUnsupportedType
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.era.common.MinecraftEra
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MappingsType
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.bedrock.versions.mappingsType
import io.github.recrafter.crafter.extensions.*
import io.github.recrafter.crafter.helpers.AccessConfigHelper
import io.github.recrafter.crafter.loaders.common.ModLoader
import io.github.recrafter.crafter.models.Mod
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.dependencies
import java.io.File

abstract class FabricFamilyModLoader(val loader: ModLoaderType) : ModLoader() {

    override fun getAccessConfigPreset(): String = AccessConfigHelper.WIDENER_PRESET

    override fun configurePlugin(mod: Mod, project: Project, sides: Set<ModSide>, accessConfig: File) = with(project) {
        val runDirectory = projectDirectory.resolve(mod.runDirectoryName)
        fabric {
            runs {
                ModSide.values().forEach { side ->
                    named(side.getName()) {
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
                        if (mod.minecraftVersion.getEra() < MinecraftEra.ALPHA) {
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
            accessWidenerPath = accessConfig
        }
        if (mod.minecraftVersion.mappingsType != MappingsType.MERGED) {
            val splitSide = sides.single()
            fabric {
                when (splitSide) {
                    ModSide.CLIENT -> clientOnlyMinecraftJar()
                    ModSide.SERVER -> serverOnlyMinecraftJar()
                }
            }
            if (loader == ModLoaderType.ORNITHE) {
                ornithe {
                    @Suppress("DEPRECATION")
                    when (splitSide) {
                        ModSide.CLIENT -> clientOnlyMappings()
                        ModSide.SERVER -> serverOnlyMappings()
                    }
                }
            }
        }
        restoreDependencyResolutionRepositories()
        dependencies {
            minecraft("com.mojang", "minecraft", mod.minecraftVersion.asString())
            modImplementation("net.fabricmc", "fabric-loader", mod.versions.loader)
            mappings(resolveMappings(project, mod))
        }
    }

    private fun resolveMappings(project: Project, mod: Mod): Any = with(project) {
        when (loader) {
            ModLoaderType.FABRIC -> buildArtifactCoordinates(
                "net.fabricmc",
                "yarn",
                mod.versions.mappings.orEmpty(),
                "v2"
            )

            ModLoaderType.LEGACY_FABRIC -> legacyFabric.yarn(mod.versions.mappingsMinecraft, mod.versions.mappings)
            ModLoaderType.ORNITHE -> ornithe.featherMappings(mod.versions.mappings)
            else -> failWithUnsupportedType(loader::class)
        }
    }
}
