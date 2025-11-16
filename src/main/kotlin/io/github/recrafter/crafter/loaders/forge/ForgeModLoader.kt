package io.github.recrafter.crafter.loaders.forge

import io.github.diskria.gradle.utils.extensions.*
import io.github.diskria.gradle.utils.helpers.jvm.JvmArguments
import io.github.diskria.gradle.utils.helpers.jvm.Size
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.properties.autoNamedProperty
import io.github.diskria.kotlin.utils.words.PascalCase
import io.github.recrafter.bedrock.era.Release
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.bedrock.versions.compareTo
import io.github.recrafter.crafter.extensions.forge
import io.github.recrafter.crafter.extensions.lazyConfigure
import io.github.recrafter.crafter.extensions.minecraft
import io.github.recrafter.crafter.loaders.common.ModLoader
import io.github.recrafter.crafter.models.Mod
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.Actions.with
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import java.io.File

object ForgeModLoader : ModLoader() {

    override fun getPrepareRunTasks(pluginProject: Project, side: ModSide): List<Task> =
        listOfNotNull(pluginProject.getTaskOrNull("prepareRun" + side.getName(PascalCase)))

    override fun isResourcePackConfigRequired(): Boolean = true

    override fun configurePlugin(
        mod: Mod,
        project: Project,
        sides: Set<ModSide>,
        accessConfig: File,
    ) = with(project) {
        val runDirectory = project.projectDirectory.resolve(mod.runDirectoryName)
        forge {
            ensurePluginApplied("org.parchmentmc.librarian.forgegradle")
            reobf = mod.minecraftVersion < Release.V_1_20_6
            mappings(
                "parchment",
                mod.versions.mappings + Constants.Char.HYPHEN + mod.versions.mappingsMinecraft
            )
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
            val forgeVersion = "${mod.minecraftVersion.asString()}-${mod.versions.loader}"
            minecraft("net.minecraftforge", "forge", forgeVersion)
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
            }
            lazyConfigure<Task>("makeSrcDirs") { disable() }
        }
    }
}
