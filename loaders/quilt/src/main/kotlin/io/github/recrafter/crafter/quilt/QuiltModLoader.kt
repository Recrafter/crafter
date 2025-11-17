package io.github.recrafter.crafter.quilt

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.gradle.utils.extensions.projectDirectory
import io.github.diskria.gradle.utils.extensions.restoreDependencyResolutionRepositories
import io.github.diskria.gradle.utils.helpers.jvm.JvmArguments
import io.github.diskria.gradle.utils.helpers.jvm.Size
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.`Title Case`
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.era.common.MinecraftEra
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.ModLoader
import io.github.recrafter.crafter.core.extensions.mappings
import io.github.recrafter.crafter.core.extensions.minecraft
import io.github.recrafter.crafter.core.extensions.modImplementation
import io.github.recrafter.crafter.quilt.extensions.quilt
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.dependencies
import java.io.File

object QuiltModLoader : ModLoader {

    override fun configurePlugin(
        mod: Mod,
        project: Project,
        sides: Set<ModSide>,
        accessConfig: File,
    ) = with(project) {
        gradleError("Quilt mod loader is unsupported yet")
        val runDirectory = projectDirectory.resolve(mod.runDirectoryName)
        quilt {
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
            accessWidenerPath = accessConfig
        }
        restoreDependencyResolutionRepositories()
        dependencies {
            minecraft("com.mojang", "minecraft", mod.minecraftVersion.asString())
            modImplementation("org.quiltmc", "quilt-loader", mod.versions.loader)
            mappings("org.quiltmc", "quilt-mappings", mod.versions.mappings.orEmpty(), "intermediary-v2")
        }
    }
}
