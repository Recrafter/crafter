package io.github.recrafter.crafter

import io.github.diskria.gradle.utils.extensions.*
import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.ensureDirectoryExists
import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.generics.addIfNotNull
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.mappers.toEnum
import io.github.diskria.kotlin.utils.extensions.toBooleanOrNull
import io.github.recrafter.bedrock.extensions.getModRecipe
import io.github.recrafter.bedrock.loaders.ModLoaderFamily
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.MinecraftVersionRange
import io.github.recrafter.crafter.babric.sync.BarnMappingsSync
import io.github.recrafter.crafter.cli.shell.ShellHelper
import io.github.recrafter.crafter.core.CrafterConstants
import io.github.recrafter.crafter.core.LoaderMetadata
import io.github.recrafter.crafter.core.ModMetadata
import io.github.recrafter.crafter.core.extensions.family
import io.github.recrafter.crafter.core.extensions.getRunTaskName
import io.github.recrafter.crafter.core.extensions.isLoaderProject
import io.github.recrafter.crafter.core.extensions.kotlinApply
import io.github.recrafter.crafter.core.helpers.MixinsHelper
import io.github.recrafter.crafter.core.helpers.server.EulaHelper
import io.github.recrafter.crafter.core.helpers.server.ServerOperatorsHelper
import io.github.recrafter.crafter.core.helpers.server.ServerPropertiesHelper
import io.github.recrafter.crafter.core.tasks.public.CraftClientTask
import io.github.recrafter.crafter.core.tasks.public.CraftServerTask
import io.github.recrafter.crafter.extensions.gradle.CrafterExtension
import io.github.recrafter.crafter.extensions.mappers.mapToAdapter
import io.github.recrafter.crafter.fabric.sync.FabricFamilyLoaderSync
import io.github.recrafter.crafter.forge.sync.ForgeLoaderSync
import io.github.recrafter.crafter.legacy_fabric.sync.LegacyYarnMappingsSync
import io.github.recrafter.crafter.neoforge.sync.NeoForgeLoaderSync
import io.github.recrafter.crafter.ornithe.sync.FeatherMappingsSync
import io.github.recrafter.crafter.tasks.internal.CraftModConfigTask
import io.github.recrafter.crafter.tasks.public.InstallCrafterCLITask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.kotlin.dsl.*
import org.gradle.util.GradleVersion

@Suppress("unused")
class CrafterGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        if (!project.isRootProject()) {
            gradleError("The ${CrafterConstants.PLUGIN_NAME} must be applied only to the root project")
        }
        tasks {
            named<Wrapper>("wrapper") {
                gradleVersion = RECOMMENDED_GRADLE_VERSION
                distributionType = Wrapper.DistributionType.BIN
            }
        }
        checkEnvironment(project)

        val extension = registerExtension<CrafterExtension>()
        extension.onConfigurationReady {
            val modMetadata = extension.buildModMetadata(getModRecipe())
            registerTask<InstallCrafterCLITask> {
                this.modMetadata.set(modMetadata)
            }
            InstallCrafterCLITask.saveGradleFingerprint(project, modMetadata)

            val isBisectFlowRunning = System.getProperty("bisect")?.toBooleanOrNull() == true
            val loaderProjects = children.filter { it.isLoaderProject() }
            if (isBisectFlowRunning) {
                val bisectTarget: MinecraftVersion by extra
                val loaderProject = loaderProjects.single()
                val modProject = loaderProject.children.single()
                configureModProject(loaderProject, modProject, modMetadata, MinecraftVersionRange.of(bisectTarget))
            } else {
                loaderProjects.forEach { loaderProject ->
                    loaderProject.children
                        .associateWith { MinecraftVersionRange.parse(it.name, PROJECT_NAME_SEPARATOR) }
                        .forEach { (modProject, versionRange) ->
                            configureModProject(loaderProject, modProject, modMetadata, versionRange)
                        }
                }
            }
        }
        afterEvaluate {
            extension.requireConfiguration()
        }
    }

    private fun resolveLoaderMetadata(
        loader: ModLoaderType,
        project: Project,
        version: MinecraftVersion
    ): LoaderMetadata =
        when (loader.family) {
            ModLoaderFamily.FABRIC -> {
                LoaderMetadata(
                    loaderVersion = FabricFamilyLoaderSync(loader).getLatestVersion(project, version),
                    mappingsVersion = when (loader) {
                        ModLoaderType.LEGACY_FABRIC -> LegacyYarnMappingsSync.getLatestVersion(project, version)
                        ModLoaderType.BABRIC -> BarnMappingsSync.getLatestVersion(project, version)
                        ModLoaderType.ORNITHE -> FeatherMappingsSync.getLatestVersion(project, version)
                        else -> null
                    },
                    minecraftVersion = when (loader) {
                        ModLoaderType.LEGACY_FABRIC -> LegacyYarnMappingsSync.getMinecraftVersion(project, version)
                        else -> version
                    }
                )
            }

            ModLoaderFamily.FORGE -> {
                val loaderVersion = when (loader) {
                    ModLoaderType.FORGE -> ForgeLoaderSync.getLatestVersion(project, version)
                    ModLoaderType.NEOFORGE -> NeoForgeLoaderSync.getLatestVersion(project, version)
                    else -> TODO()
                }
                LoaderMetadata(
                    loaderVersion = loaderVersion,
                    minecraftVersion = version
                )
            }
        }

    private fun configureModProject(
        loaderProject: Project,
        modProject: Project,
        modMetadata: ModMetadata,
        versionRange: MinecraftVersionRange,
    ) {
        val minVersion = versionRange.min
        val maxVersion = versionRange.max
        val loader = loaderProject.name.toEnum<ModLoaderType>(`kebab-case`)
        val loaderAdapter = loader.mapToAdapter()
        val loaderMetadata = resolveLoaderMetadata(loader, loaderProject, minVersion)
        val mod = modMetadata.toMod(loader, minVersion, maxVersion, loaderMetadata)
        with(modProject) {
            ensurePluginApplied("org.jetbrains.kotlin.jvm")
            group = mod.namespace
            version = mod.archiveVersion
        }
        val sideProjects = mod.environment.sides.associateWith { side ->
            modProject.children.first { it.name == side.getName() }.kotlinApply {
                ensurePluginApplied("org.jetbrains.kotlin.jvm")
            }
        }
        val runDirectory = modProject.projectDirectory.resolve(mod.runDirectoryName)
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
            val pluginMainSourceSet = modProject.sourceSets.main
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
        val craftModConfigTask = modProject.registerTask<CraftModConfigTask> {
            this.mod.set(mod)
            outputFile = getTempFile(mod.loaderConfigPath)
        }
        loaderAdapter.configureInternal(mod, modProject, runDirectory, sideProjects, craftModConfigTask)
        ModSide.values().forEach { side ->
            when (side) {
                ModSide.CLIENT -> modProject.registerTask<CraftClientTask>()
                ModSide.SERVER -> modProject.registerTask<CraftServerTask>()
            } {
                dependsSequentiallyOn(
                    buildList {
                        add(modProject.tasks.build.get())
                        addAll(loaderAdapter.getPrepareRunTasks(modProject, side))
                        addIfNotNull(modProject.getTaskOrNull(side.getRunTaskName()))
                    }
                )
            }
        }
    }

    private fun checkEnvironment(project: Project) {
        val currentVersion = GradleVersion.current().version
        if (currentVersion != RECOMMENDED_GRADLE_VERSION) {
            project.logger.warn(
                buildString {
                    appendLine(
                        "Detected Gradle version $currentVersion may not be fully compatible " +
                                "with ${CrafterConstants.PLUGIN_NAME}."
                    )
                    appendLine("Recommended Gradle version: $RECOMMENDED_GRADLE_VERSION")
                    append("To update, run: ${ShellHelper.gradleTaskCommand("wrapper")}")
                }
            )
        }
    }

    companion object {
        private const val RECOMMENDED_GRADLE_VERSION: String = "8.14.3"
        private const val PROJECT_NAME_SEPARATOR: String = "--"
        private const val BISECT_FLOW_FLAG = "bisect"
    }
}
