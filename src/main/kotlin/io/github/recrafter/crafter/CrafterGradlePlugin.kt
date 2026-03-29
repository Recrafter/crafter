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
import io.github.diskria.kotlin.utils.extensions.wrapWithSingleQuote
import io.github.recrafter.bedrock.crafter.CrafterConstants
import io.github.recrafter.bedrock.crafter.CrafterFlow
import io.github.recrafter.bedrock.extensions.getModRecipe
import io.github.recrafter.bedrock.loaders.ModLoaderFamily
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.MinecraftVersionRange
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.cli.bash.utils.Cmd
import io.github.recrafter.crafter.core.CrafterTasks
import io.github.recrafter.crafter.core.LoaderCompatibility
import io.github.recrafter.crafter.core.LoaderMetadata
import io.github.recrafter.crafter.core.ModMetadata
import io.github.recrafter.crafter.core.extensions.*
import io.github.recrafter.crafter.core.helpers.server.EulaHelper
import io.github.recrafter.crafter.core.helpers.server.ServerOperatorsHelper
import io.github.recrafter.crafter.core.helpers.server.ServerPropertiesHelper
import io.github.recrafter.crafter.extensions.gradle.CrafterExtension
import io.github.recrafter.crafter.extensions.mappers.mapToAdapter
import io.github.recrafter.crafter.loaders.babric.sync.BinyMappingsSync
import io.github.recrafter.crafter.loaders.fabric.sync.FabricFamilyLoaderSync
import io.github.recrafter.crafter.loaders.forge.sync.ForgeLoaderSync
import io.github.recrafter.crafter.loaders.legacy_fabric.sync.LegacyYarnMappingsSync
import io.github.recrafter.crafter.loaders.neoforge.sync.NeoForgeLoaderSync
import io.github.recrafter.crafter.loaders.ornithe.sync.FeatherMappingsSync
import io.github.recrafter.crafter.tasks.CraftClientTask
import io.github.recrafter.crafter.tasks.CraftServerTask
import io.github.recrafter.crafter.tasks.InstallCrafterCLITask
import io.github.recrafter.crafter.tasks.internal.CraftLoaderConfigTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.kotlin.dsl.*
import org.gradle.util.GradleVersion

class CrafterGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        requireGradle(project.isRootProject()) {
            "The ${CrafterConstants.PLUGIN_NAME} Gradle Plugin must be applied only to the root project."
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
                    children.filter { it.isLoaderProject() }.forEach { loaderProject ->
                        val modProjects = loaderProject.children.associateWith {
                            MinecraftVersionRange.parse(it.name, MinecraftVersionRange.PROJECT_NAME_SEPARATOR)
                        }
                        modProjects.forEach { (modProject, versionRange) ->
                            configureModProject(loaderProject, modProject, modMetadata, versionRange)
                        }
                        loaderProject.registerTask<CraftClientTask> {
                            dependsSequentiallyOn(
                                modProjects.entries
                                    .sortedWith(compareByDescending(MinecraftVersion.COMPARATOR) { it.value.min })
                                    .map { it.key.getTask<CraftClientTask>() }
                            )
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
        tasks.register<Copy>("release") {
            group = CrafterTasks.PUBLIC_GROUP

            val releaseDir = layout.buildDirectory.dir("release")
            destinationDir = releaseDir.get().asFile

            val allBuildTasks = allprojects.map { project ->
                if (ModSide.entries.any { it.name.equals(project.name, ignoreCase = true) }) {
                    files()
                } else {
                    project.tasks.matching {
                        it.name == "shadowJar" || it.name == "remapJar" || it.name == "jar"
                    }
                }
            }

            dependsOn(allBuildTasks)

            from(allBuildTasks) {
                include("*.jar")
                exclude("**/*-sources.jar", "**/*-dev.jar")

                eachFile {
                    path = name
                }
            }

            doFirst {
                if (destinationDir.exists()) {
                    destinationDir.deleteRecursively()
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
        version: MinecraftVersion,
    ): LoaderMetadata =
        when (loader.family) {
            ModLoaderFamily.FABRIC -> {
                LoaderMetadata(
                    loaderVersion = FabricFamilyLoaderSync(loader).getLatestVersion(project, version),
                    mappingsVersion = when (loader) {
                        ModLoaderType.LEGACY_FABRIC -> LegacyYarnMappingsSync.getLatestVersion(project, version)
                        ModLoaderType.BABRIC -> BinyMappingsSync.getLatestVersion(project, version)
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
        requireGradle(loader.supportedVersions.contains(minVersion) && loader.supportedVersions.contains(maxVersion)) {
            buildString {
                appendLine(
                    "Mod project ${modProject.path.wrapWithSingleQuote()} targets Minecraft versions " +
                        "${versionRange.asString()}, which are outside the supported range for " +
                        "loader ${loader.displayName}."
                )
                appendLine("Supported versions: ${loader.supportedVersions.joinToString { it.asString() }}.")
            }
        }
        if (!versionRange.isSingleVersion()) {
            val candidates = versionRange.expand().toMutableList().apply { removeFirst() }
            val checkpoints = LoaderCompatibility.getCheckpoints(loader)
            requireGradle(candidates.intersect(checkpoints.toSet()).isEmpty()) {
                "Mod project ${modProject.path.wrapWithSingleQuote()} targets Minecraft versions " +
                    "${versionRange.asString()}, intersect compatibility checkpoints."
            }
        }
        val loaderMetadata = resolveLoaderMetadata(loader, loaderProject, minVersion)
        val mod = modMetadata.toMod(loader, minVersion, maxVersion, loaderMetadata)
        with(modProject) {
            ensureKotlinPluginApplied()
            ensureKotlinSerializationPluginApplied()
            ensureKspPluginApplied()
            group = mod.namespace
            version = mod.archiveVersion
        }
        val sideProjects = mod.environment.sides.associateWith { side ->
            modProject.children.single { it.name == side.getName() }.ensureKotlinPluginApplied()
        }
        sideProjects.values.forEach { it.sourceSets.main.addToClasspath(modProject.sourceSets.main, withOutput = true) }
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
        val accessorConfig = modProject.getGeneratedDirectory().resolve("ksp/main/resources/" + mod.accessorConfigName)
        val craftLoaderConfigTask = modProject.registerTask<CraftLoaderConfigTask> {
            this.mod.set(mod)
            this.accessorConfig = accessorConfig
            outputFile = getTempFile(mod.loaderConfigPath)
        }
        with(modProject) {
            tasks {
                processResources {
                    copyTaskOutput(craftLoaderConfigTask, mod.loaderConfigPath)
                }
            }
        }
        loader.mapToAdapter().configureInternal(mod, modProject, runDirectory, accessorConfig, sideProjects)
        ModSide.entries.forEach { side ->
            when (side) {
                ModSide.CLIENT -> modProject.registerTask<CraftClientTask>()
                ModSide.SERVER -> modProject.registerTask<CraftServerTask>()
            } {
                dependsSequentiallyOn(
                    buildList {
                        add(modProject.tasks.build.get())
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
                            "with ${CrafterConstants.PLUGIN_NAME} Gradle Plugin."
                    )
                    appendLine("Recommended Gradle version: $RECOMMENDED_GRADLE_VERSION")
                    append("To update, run: ${Cmd.gradleTask("wrapper")}")
                }
            )
        }
    }

    companion object {
        private const val RECOMMENDED_GRADLE_VERSION: String = "9.4.1"
    }
}
