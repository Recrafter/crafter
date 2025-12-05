package io.github.recrafter.crafter

import io.github.diskria.gradle.utils.extensions.*
import io.github.diskria.gradle.utils.extensions.common.requireGradle
import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.ensureDirectoryExists
import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.generics.addIfNotNull
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.mappers.toEnum
import io.github.recrafter.bedrock.crafter.CrafterConstants
import io.github.recrafter.bedrock.crafter.CrafterFlow
import io.github.recrafter.bedrock.extensions.getModRecipe
import io.github.recrafter.bedrock.loaders.ModLoaderFamily
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.MinecraftVersionRange
import io.github.recrafter.bedrock.versions.contains
import io.github.recrafter.crafter.cli.bash.utils.Cmd
import io.github.recrafter.crafter.core.LoaderMetadata
import io.github.recrafter.crafter.core.ModMetadata
import io.github.recrafter.crafter.core.extensions.*
import io.github.recrafter.crafter.core.helpers.MixinsHelper
import io.github.recrafter.crafter.core.helpers.server.EulaHelper
import io.github.recrafter.crafter.core.helpers.server.ServerOperatorsHelper
import io.github.recrafter.crafter.core.helpers.server.ServerPropertiesHelper
import io.github.recrafter.crafter.extensions.gradle.CrafterExtension
import io.github.recrafter.crafter.extensions.mappers.mapToAdapter
import io.github.recrafter.crafter.loaders.babric.sync.BarnMappingsSync
import io.github.recrafter.crafter.loaders.fabric.sync.FabricFamilyLoaderSync
import io.github.recrafter.crafter.loaders.forge.sync.ForgeLoaderSync
import io.github.recrafter.crafter.loaders.legacy_fabric.sync.LegacyYarnMappingsSync
import io.github.recrafter.crafter.loaders.neoforge.sync.NeoForgeLoaderSync
import io.github.recrafter.crafter.loaders.ornithe.sync.FeatherMappingsSync
import io.github.recrafter.crafter.tasks.internal.CraftLoaderConfigTask
import io.github.recrafter.crafter.tasks.public.CraftClientTask
import io.github.recrafter.crafter.tasks.public.CraftServerTask
import io.github.recrafter.crafter.tasks.public.InstallCrafterCLITask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import org.gradle.util.GradleVersion

@Suppress("unused")
class CrafterGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        requireGradle(project.isRootProject()) {
            "The ${CrafterConstants.PLUGIN_NAME} must be applied only to the root project."
        }
        extra["fabric.loom.disableMinecraftVerification"] = "true"
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
            InstallCrafterCLITask.ensureScriptExists(project, modMetadata)

            when (val flow = CrafterFlow.detect()) {
                is CrafterFlow.Normal -> {
                    children
                        .filter { it.isLoaderProject() }
                        .forEach { loaderProject ->
                            loaderProject.children
                                .associateWith {
                                    MinecraftVersionRange.parse(
                                        it.name,
                                        MinecraftVersionRange.MOD_PROJECT_NAME_SEPARATOR
                                    )
                                }
                                .forEach { (modProject, versionRange) ->
                                    configureModProject(loaderProject, modProject, modMetadata, versionRange)
                                }
                        }
                }

                is CrafterFlow.Single -> {
                    val loaderProject = children.single { it.name == flow.loader.getName(`kebab-case`) }
                    val modProject = loaderProject.children.single { it.name == flow.modProjectName }
                    configureModProject(loaderProject, modProject, modMetadata, MinecraftVersionRange.of(flow.version))
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
                    else -> failWithInvalidValue(loader)
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
        val supportedVersionRange = loader.supportedVersionRange
        requireGradle(supportedVersionRange.contains(minVersion) && supportedVersionRange.contains(maxVersion)) {
            "Mod project ${modProject.path} uses Minecraft versions outside the supported range " +
                    "for loader ${loader.displayName}: required ${supportedVersionRange.asString("-")}, " +
                    "but found ${versionRange.asString("-")}."
        }
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
        val modMainSourceSet = modProject.sourceSets.main
        sideProjects.values.forEach { sideProject ->
            sideProject.sourceSets.main.addToClasspath(modMainSourceSet, withOutput = true)
            val mixins = sideProject.sourceSets.create(MixinsHelper.MIXINS_NAME).apply {
                addToClasspath(modMainSourceSet, withOutput = true)
            }
            with(sideProject) {
                tasks {
                    jar {
                        from(mixins.output)
                    }
                }
            }
        }
        val craftLoaderConfigTask = modProject.registerTask<CraftLoaderConfigTask> {
            this.mod.set(mod)
            outputFile = getTempFile(mod.loaderConfigPath)
        }
        loaderAdapter.configureInternal(mod, modProject, runDirectory, sideProjects, craftLoaderConfigTask)
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
                    append("To update, run: ${Cmd.gradleTask("wrapper")}")
                }
            )
        }
    }

    companion object {
        private const val RECOMMENDED_GRADLE_VERSION: String = "8.14.3"
    }
}
