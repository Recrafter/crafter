package io.github.recrafter.crafter.loaders.forge

import io.github.diskria.gradle.utils.extensions.common.artifact
import io.github.diskria.gradle.utils.extensions.getTaskOrNull
import io.github.diskria.gradle.utils.extensions.jar
import io.github.diskria.gradle.utils.helpers.jvm.JvmArguments
import io.github.diskria.gradle.utils.helpers.jvm.Size
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.properties.autoNamedProperty
import io.github.diskria.kotlin.utils.words.PascalCase
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.ModLoaderAdapter
import io.github.recrafter.crafter.core.extensions.groupLoaderTasks
import io.github.recrafter.crafter.core.extensions.lazyDisable
import io.github.recrafter.crafter.core.extensions.minecraft
import io.github.recrafter.crafter.loaders.forge.extensions.forge
import net.minecraftforge.gradle.common.util.RunConfig
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import java.io.File

object ForgeAdapter : ModLoaderAdapter() {

    override fun getPrepareRunTasks(loaderProject: Project, side: ModSide): List<Task> =
        listOfNotNull(
            loaderProject.getTaskOrNull("prepareRun" + side.getName(PascalCase) + "Compile"),
        )

    override fun isDataPackConfigRequired(): Boolean = true

    override fun configurePlugin(
        mod: Mod,
        project: Project,
        runDirectory: File,
        accessConfig: File,
        isRunConfigurationsDisabled: Boolean,
    ) = with(project) {
        forge {
            mappings("official", mod.minecraftVersion.asString())

            reobf = mod.isReobfNeeded
            setAccessTransformer(accessConfig)
            runs {
                ModSide.values().forEach { side ->
                    create(side.getName()) {
                        workingDirectory(runDirectory.resolve(side.getName()))
                        val memoryRange = when (side) {
                            ModSide.CLIENT -> 2..4
                            ModSide.SERVER -> 4..8
                        }
                        jvmArgs(
                            *JvmArguments.memory(memoryRange, Size.GIGABYTES),
                            JvmArguments.property("mixin.debug.export", true),
                        )
                        args(
                            *JvmArguments.program("mixin.config", mod.mixinsConfigPath),
                        )
                        when (side) {
                            ModSide.CLIENT -> args(
                                *JvmArguments.program("username", mod.player),
                            )

                            ModSide.SERVER -> args(
                                *JvmArguments.program("nogui"),
                            )
                        }
                    }
                }
            }
        }
        dependencies {
            val forgeVersion = "${mod.minecraftVersion.asString()}-${mod.loaderMetadata.loaderVersion}"
            val minecraftArtifact = artifact("net.minecraftforge", "forge", forgeVersion)
            mod.log(project, "Minecraft: $minecraftArtifact")
            minecraft(minecraftArtifact)
        }
        tasks {
            jar {
                manifest {
                    val mixinConfigs by mod.mixinsConfigPath.autoNamedProperty(PascalCase)
                    attributes(
                        listOf(
                            mixinConfigs,
                        ).associate { it.name to it.value }
                    )
                }
                if (mod.isReobfNeeded) {
                    finalizedBy("reobfJar")
                }
            }
            lazyDisable("makeSrcDirs")
            if (isRunConfigurationsDisabled) {
                lazyDisable("genIntellijRuns")
            }
        }
        groupLoaderTasks(
            loaderPackageNamePrefixes = listOf("net.minecraftforge.gradle"),
            taskGroups = listOf(RunConfig.PREPARE_RUNS_GROUP),
            taskNames = listOf("makeSrcDirs"),
            loader = ModLoaderType.FORGE,
        )
    }
}
