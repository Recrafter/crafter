package io.github.recrafter.crafter.loaders.forge

import io.github.diskria.gradle.utils.extensions.projectDirectory
import io.github.diskria.gradle.utils.helpers.jvm.JvmArguments
import io.github.diskria.gradle.utils.helpers.jvm.Size
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.extensions.neoforge
import io.github.recrafter.crafter.loaders.common.ModLoader
import io.github.recrafter.crafter.models.Mod
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import java.io.File

object NeoForgeModLoader : ModLoader() {

    override fun configurePlugin(
        mod: Mod,
        project: Project,
        sides: Set<ModSide>,
        accessConfig: File,
    ) = with(project) {
        val runDirectory = project.projectDirectory.resolve(mod.runDirectoryName)
        neoforge {
            version = mod.versions.loader
            parchment {
                minecraftVersion = mod.versions.mappingsMinecraft
                mappingsVersion = mod.versions.mappings
            }
            setAccessTransformers(accessConfig)
            runs {
                ModSide.values().forEach { side ->
                    create(side.getName()) {
                        gameDirectory = runDirectory
                        val memoryRange = when (side) {
                            ModSide.CLIENT -> 2..4
                            ModSide.SERVER -> 4..8
                        }
                        jvmArguments.addAll(
                            *JvmArguments.memory(memoryRange, Size.GIGABYTES),
                            JvmArguments.property("mixin.debug.export", true),
                        )
                        when (side) {
                            ModSide.CLIENT -> {
                                client()
                                programArguments.addAll(
                                    *JvmArguments.program("username", mod.player),
                                )
                            }

                            ModSide.SERVER -> {
                                server()
                                programArguments.addAll(
                                    *JvmArguments.program("nogui"),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
