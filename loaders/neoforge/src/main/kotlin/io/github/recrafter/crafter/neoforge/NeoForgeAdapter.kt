package io.github.recrafter.crafter.neoforge

import io.github.diskria.gradle.utils.helpers.jvm.JvmArguments
import io.github.diskria.gradle.utils.helpers.jvm.Size
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.ModLoaderAdapter
import io.github.recrafter.crafter.core.extensions.groupLoaderTasks
import io.github.recrafter.crafter.neoforge.extensions.neoforge
import net.neoforged.moddevgradle.internal.Branding
import org.gradle.api.Project
import java.io.File

object NeoForgeAdapter : ModLoaderAdapter() {

    override fun configurePlugin(
        mod: Mod,
        project: Project,
        runDirectory: File,
        accessConfig: File,
        isRunConfigurationsDisabled: Boolean,
    ) = with(project) {
        neoforge {
            version = mod.loaderMetadata.loaderVersion
            setAccessTransformers(accessConfig)
            runs {
                ModSide.values().forEach { side ->
                    create(side.getName()) {
                        if (isRunConfigurationsDisabled) {
                            ideName.set(Constants.Char.EMPTY)
                        }
                        gameDirectory.set(runDirectory.resolve(side.getName()))
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
        groupLoaderTasks(
            loaderPackageNamePrefixes = listOf("net.neoforged.moddevgradle"),
            taskGroups = listOf(Branding.MDG.publicTaskGroup, Branding.MDG.internalTaskGroup),
            loader = ModLoaderType.NEOFORGE,
        )
    }
}
