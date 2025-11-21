package io.github.recrafter.crafter

import io.github.diskria.gradle.utils.extensions.*
import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.kotlin.utils.extensions.asFileOrNull
import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.common.snake_case
import io.github.diskria.kotlin.utils.extensions.ensureDirectoryExists
import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.generics.addIfNotNull
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.mappers.toEnumOrNull
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.recrafter.bedrock.extensions.getModRecipe
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MinecraftVersionRange
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.babric.sync.BabricMappingsSynchronizer
import io.github.recrafter.crafter.core.*
import io.github.recrafter.crafter.core.extensions.getRunTaskName
import io.github.recrafter.crafter.core.extensions.kotlinApply
import io.github.recrafter.crafter.core.helpers.MixinsHelper
import io.github.recrafter.crafter.core.helpers.server.EulaHelper
import io.github.recrafter.crafter.core.helpers.server.ServerOperatorsHelper
import io.github.recrafter.crafter.core.helpers.server.ServerPropertiesHelper
import io.github.recrafter.crafter.core.tasks.public.CraftClientTask
import io.github.recrafter.crafter.core.tasks.public.CraftServerTask
import io.github.recrafter.crafter.extensions.gradle.CrafterExtension
import io.github.recrafter.crafter.extensions.mappers.mapToAdapter
import io.github.recrafter.crafter.fabric.sync.FabricFamilyLoaderSynchronizer
import io.github.recrafter.crafter.fabric.sync.FabricMappingsSynchronizer
import io.github.recrafter.crafter.forge.sync.ForgeLoaderSynchronizer
import io.github.recrafter.crafter.legacy_fabric.sync.LegacyFabricMappingsSynchronizer
import io.github.recrafter.crafter.neoforge.sync.NeoForgeLoaderSynchronizer
import io.github.recrafter.crafter.ornithe.sync.OrnitheMappingsSynchronizer
import io.github.recrafter.crafter.quilt.sync.QuiltMappingsSynchronizer
import io.github.recrafter.crafter.tasks.internal.CraftModConfigTask
import io.github.recrafter.crafter.tasks.public.InstallCrafterCLITask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import java.io.File

class CrafterGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        if (!project.isRootProject()) {
            gradleError("The ${CrafterConstants.PLUGIN_NAME} must be applied only to the root project")
        }

        val extension = registerExtension<CrafterExtension>()
        extension.onConfigurationReady {
            val modMetadata = extension.buildModMetadata(getModRecipe())
            registerTask<InstallCrafterCLITask> {
                this.modMetadata.set(modMetadata)
            }
            InstallCrafterCLITask.saveGradleFingerprint(project, modMetadata)

            tasks {
                named<Wrapper>("wrapper") {
                    setGradleVersion("8.14.3")
                    distributionType = Wrapper.DistributionType.ALL
                }
            }
            children.forEach { loaderProject ->
                configureVersionProjects(loaderProject, modMetadata)
            }
        }
        afterEvaluate {
            extension.requireConfiguration()
        }
    }

    private fun configureVersionProjects(loaderProject: Project, modMetadata: ModMetadata) {
        val loaderType = loaderProject.name.setCase(`kebab-case`, snake_case).toEnumOrNull<ModLoaderType>() ?: return
        val loader = loaderType.mapToAdapter()
        val versionProjects = loaderProject.children
        val projectVersions = versionProjects.associateWith { MinecraftVersionRange.parse(it.name) }
        projectVersions.forEach { (versionProject, range) ->
            val minecraftVersion = range.min
            val maxMinecraftVersion = range.max

            val loaderSynchronizer = FabricFamilyLoaderSynchronizer(loaderType)
            val versionsMetadata = when (loaderType) {
                ModLoaderType.FABRIC -> VersionsMetadata(
                    loader = loaderSynchronizer.getLatestVersion(loaderProject, minecraftVersion),
                    mappings = FabricMappingsSynchronizer.getLatestVersion(loaderProject, minecraftVersion),
                )

                ModLoaderType.QUILT -> VersionsMetadata(
                    loader = loaderSynchronizer.getLatestVersion(loaderProject, minecraftVersion),
                    mappings = QuiltMappingsSynchronizer.getLatestVersion(loaderProject, minecraftVersion),
                )

                ModLoaderType.LEGACY_FABRIC -> {
                    val component = LegacyFabricMappingsSynchronizer.getLatestComponent(loaderProject, minecraftVersion)
                    VersionsMetadata(
                        loader = loaderSynchronizer.getLatestVersion(loaderProject, minecraftVersion),
                        mappings = component.latestVersion,
                        mappingsMinecraft = component.minecraftVersion.asString(),
                    )
                }

                ModLoaderType.ORNITHE -> {
                    VersionsMetadata(
                        loader = loaderSynchronizer.getLatestVersion(loaderProject, minecraftVersion),
                        mappings = OrnitheMappingsSynchronizer.getLatestVersion(loaderProject, minecraftVersion),
                    )
                }

                ModLoaderType.BABRIC -> VersionsMetadata(
                    loader = loaderSynchronizer.getLatestVersion(loaderProject, minecraftVersion),
                    mappings = BabricMappingsSynchronizer.getLatestVersion(loaderProject, minecraftVersion),
                )

                ModLoaderType.FORGE -> {
                    VersionsMetadata(
                        loader = ForgeLoaderSynchronizer.getLatestVersion(loaderProject, minecraftVersion),
                    )
                }

                ModLoaderType.NEOFORGE -> {
                    VersionsMetadata(
                        loader = NeoForgeLoaderSynchronizer.getLatestVersion(loaderProject, minecraftVersion),
                    )
                }
            }
            val mod = modMetadata.toMod(loaderType, minecraftVersion, maxMinecraftVersion, versionsMetadata)
            val iconFile = loaderProject.projectDirectory.parentFile?.resolve(mod.iconFileName)?.asFileOrNull()
            configureVersionProject(mod, loader, iconFile, versionProject, mod.archiveVersion)
        }
    }

    private fun configureVersionProject(
        mod: Mod,
        loaderAdapter: ModLoaderAdapter,
        iconFile: File?,
        project: Project,
        archiveVersion: String,
    ) {
        with(project) {
            ensurePluginApplied("org.jetbrains.kotlin.jvm")
            group = mod.namespace
            version = archiveVersion
        }
        val sideProjects = mod.environment.sides.associateWith { side ->
            project.children.first { it.name == side.getName() }.kotlinApply {
                ensurePluginApplied("org.jetbrains.kotlin.jvm")
            }
        }
        val runDirectory = project.projectDirectory.resolve(mod.runDirectoryName)
        runDirectory.resolve(ModSide.SERVER.getName()).ensureDirectoryExists {
            resolve(EulaHelper.FILE_NAME).ensureFileExists {
                writeText(EulaHelper.buildPreset(mod))
            }
            resolve(ServerPropertiesHelper.FILE_NAME).ensureFileExists {
                writeText(ServerPropertiesHelper.buildPreset(mod))
            }
            resolve(ServerOperatorsHelper.FILE_NAME).ensureFileExists {
                writeText(ServerOperatorsHelper.buildPreset(mod))
            }
        }
        sideProjects.values.forEach { sideProject ->
            val pluginMainSourceSet = project.sourceSets.main
            sideProject.sourceSets.main.addToClasspath(pluginMainSourceSet, withOutput = true)
            val mixins = sideProject.sourceSets.create(MixinsHelper.MIXINS_NAME).apply {
                addToClasspath(pluginMainSourceSet, withOutput = true)
            }
            with(sideProject) {
                tasks {
                    jar {
                        from(mixins.output)
                    }
                }
            }
        }
        val craftModConfigTask = project.registerTask<CraftModConfigTask> {
            this.mod.set(mod)
            outputFile = getTempFile(mod.loaderConfigPath)
        }
        loaderAdapter.configureInternal(mod, iconFile, project, runDirectory, sideProjects, craftModConfigTask)
        ModSide.values().forEach { side ->
            when (side) {
                ModSide.CLIENT -> project.registerTask<CraftClientTask>()
                ModSide.SERVER -> project.registerTask<CraftServerTask>()
            } {
                dependsSequentiallyOn(
                    buildList {
                        add(project.tasks.build.get())
                        addAll(loaderAdapter.getPrepareRunTasks(project, side))
                        addIfNotNull(project.getTaskOrNull(side.getRunTaskName()))
                    }
                )
            }
        }
    }
}
