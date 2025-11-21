package io.github.recrafter.crafter.fabric

import io.github.diskria.gradle.utils.extensions.common.buildArtifactCoordinates
import io.github.diskria.gradle.utils.extensions.projectDirectory
import io.github.diskria.gradle.utils.extensions.restoreDependencyResolutionRepositories
import io.github.diskria.gradle.utils.helpers.jvm.JvmArguments
import io.github.diskria.gradle.utils.helpers.jvm.Size
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.`Title Case`
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.common.failWithUnsupportedType
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.era.common.MinecraftEra
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.bedrock.versions.isIntegratedServer
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.ModLoaderAdapter
import io.github.recrafter.crafter.core.extensions.*
import io.github.recrafter.crafter.core.helpers.AccessConfigHelper
import io.github.recrafter.crafter.fabric.extensions.babric
import io.github.recrafter.crafter.fabric.extensions.legacyFabric
import io.github.recrafter.crafter.fabric.extensions.loom
import io.github.recrafter.crafter.fabric.extensions.ornithe
import io.ktor.http.*
import net.fabricmc.loom.util.Constants.TaskGroup
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import java.io.File

abstract class FabricFamilyModLoaderAdapter(val loader: ModLoaderType) : ModLoaderAdapter() {

    override fun getAccessConfigPreset(): String = AccessConfigHelper.WIDENER_PRESET

    override fun configurePlugin(
        mod: Mod,
        project: Project,
        runDirectory: File,
        accessConfig: File,
        isRunConfigurationsDisabled: Boolean,
    ) = with(project) {
        loom {
            when (loader) {
                ModLoaderType.LEGACY_FABRIC -> legacyFabric {

                }

                ModLoaderType.BABRIC -> babric {
                    customMinecraftMetadata.set(buildUrl("babric.github.io") {
                        path(
                            "manifest-polyfill",
                            fileName(mod.minecraftVersion.asString(), Constants.File.Extension.JSON)
                        )
                    }.toString())
                    intermediaryUrl.set(buildUrl("maven.glass-launcher.net") {
                        path("babric", "babric", "intermediary")
                    }.toString().run {
                        val placeholder = "%1\$s"
                        this
                            .appendPath(placeholder)
                            .appendPath(fileName("intermediary-$placeholder-v2", Constants.File.Extension.JAR))
                    })
                }

                ModLoaderType.ORNITHE -> ornithe {
                    setGeneration(2)
                }

                else -> {}
            }
            @Suppress("UnstableApiUsage")
            versionsManifests {
                if (loader == ModLoaderType.ORNITHE) {
                    add(
                        loader.displayName,
                        buildUrl("ornithemc.net") {
                            path("mc-versions", fileName("version_manifest", Constants.File.Extension.JSON))
                        }.toString()
                    )
                }
            }
            accessWidenerPath.set(accessConfig)
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
            if (loader == ModLoaderType.BABRIC || loader == ModLoaderType.ORNITHE) {
                @Suppress("UnstableApiUsage")
                mixin {
                    useLegacyMixinAp.set(true)
                    defaultRefmapName.set(mod.refmapFileName)
                }
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
            val minecraftArtifact = buildArtifactCoordinates("com.mojang", "minecraft", mod.minecraftVersion.asString())
            mod.log(project, "Minecraft: $minecraftArtifact")
            minecraft(minecraftArtifact)

            val loaderArtifact = resolveLoader(mod)
            mod.log(project, "Loader: $loaderArtifact")
            modImplementation(loaderArtifact)

            val mappingsArtifact = resolveMappings(project, mod)
            mod.log(project, "Mappings: $mappingsArtifact")
            mappings(mappingsArtifact)
        }
        groupLoaderTasks(
            loaderPackageName = listOfNotNull(
                "net.fabricmc.loom",
                when (loader) {
                    ModLoaderType.LEGACY_FABRIC -> "net.legacyfabric.legacylooming"
                    ModLoaderType.ORNITHE -> "net.ornithemc.ploceus"
                    ModLoaderType.BABRIC -> "babric"
                    else -> null
                }
            ),
            taskGroups = listOf(TaskGroup.FABRIC, TaskGroup.IDE),
            loader = loader,
        )
    }

    private fun resolveLoader(mod: Mod): String =
        when (loader) {
            ModLoaderType.QUILT -> buildArtifactCoordinates("org.quiltmc", "quilt-loader", mod.versions.loader)
            ModLoaderType.BABRIC -> buildArtifactCoordinates("babric", "fabric-loader", mod.versions.loader)
            else -> buildArtifactCoordinates("net.fabricmc", "fabric-loader", mod.versions.loader)
        }

    private fun resolveMappings(project: Project, mod: Mod) = with(project) {
        when (loader) {
            ModLoaderType.FABRIC -> buildArtifactCoordinates(
                "net.fabricmc",
                "yarn",
                mod.versions.mappings.orEmpty(),
                "v2"
            )

            ModLoaderType.QUILT -> buildArtifactCoordinates(
                "org.quiltmc",
                "quilt-mappings",
                mod.versions.mappings.orEmpty(),
                "intermediary-v2"
            )

            ModLoaderType.LEGACY_FABRIC -> legacyFabric.yarn(mod.versions.mappingsMinecraft, mod.versions.mappings)
            ModLoaderType.ORNITHE -> ornithe.featherMappings(mod.versions.mappings)
            ModLoaderType.BABRIC -> buildArtifactCoordinates("babric", "barn", mod.versions.mappings.orEmpty(), "v2")

            else -> failWithUnsupportedType(loader::class)
        }
    }
}
