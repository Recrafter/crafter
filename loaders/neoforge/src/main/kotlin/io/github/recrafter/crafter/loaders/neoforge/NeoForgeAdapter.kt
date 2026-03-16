package io.github.recrafter.crafter.loaders.neoforge

import io.github.diskria.gradle.utils.extensions.compileOnly
import io.github.diskria.gradle.utils.extensions.ksp
import io.github.diskria.gradle.utils.extensions.restoreDependencyResolutionRepositories
import io.github.diskria.gradle.utils.helpers.jvm.JvmArguments
import io.github.diskria.gradle.utils.helpers.jvm.Size
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.ModLoaderAdapter
import io.github.recrafter.crafter.core.extensions.groupLoaderTasks
import io.github.recrafter.crafter.core.extensions.shadowKotlin
import io.github.recrafter.crafter.loaders.neoforge.extensions.neoforge
import io.github.recrafter.crafter.mixins.Lapis
import net.neoforged.moddevgradle.internal.Branding
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.repositories
import java.io.File

object NeoForgeAdapter : ModLoaderAdapter() {

    override fun configurePlugin(mod: Mod, project: Project, runDirectory: File, accessorConfig: File) = with(project) {
        neoforge {
            version = mod.loaderMetadata.loaderVersion
            if (accessorConfig.isFile) {
                setAccessTransformers(accessorConfig)
            }
            runs {
                ModSide.entries.forEach { side ->
                    create(side.getName()) {
                        ideName = Constants.Char.EMPTY
                        gameDirectory = runDirectory.resolve(side.getName())
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
        dependencies {
            restoreDependencyResolutionRepositories()
            repositories {
                mavenLocal()
            }
            compileOnly(Lapis.GROUP_ID, Lapis.ANNOTATIONS_ARTIFACT_ID, Lapis.VERSION)
            ksp(Lapis.GROUP_ID, Lapis.KSP_ARTIFACT_ID, Lapis.VERSION)
        }
        shadowKotlin(mod.packageName)
        groupLoaderTasks(
            loaderPackageNamePrefixes = listOf("net.neoforged.moddevgradle"),
            taskGroups = listOf(Branding.MDG.publicTaskGroup, Branding.MDG.internalTaskGroup),
            loader = ModLoaderType.NEOFORGE,
        )
    }
}
