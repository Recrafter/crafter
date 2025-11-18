package io.github.recrafter.crafter

import io.github.diskria.gradle.utils.extensions.*
import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.asFileOrNull
import io.github.diskria.kotlin.utils.extensions.common.`Train-Case`
import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.common.snake_case
import io.github.diskria.kotlin.utils.extensions.ensureDirectoryExists
import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.generics.addIfNotNull
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.mappers.toEnumOrNull
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.autoNamedProperty
import io.github.recrafter.bedrock.extensions.getModRecipe
import io.github.recrafter.bedrock.loaders.ModLoaderFamily
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModEnvironment
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.*
import io.github.recrafter.crafter.babric.sync.BabricLoaderSynchronizer
import io.github.recrafter.crafter.babric.sync.BabricMappingsSynchronizer
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.ModLoader
import io.github.recrafter.crafter.core.ModMetadata
import io.github.recrafter.crafter.core.VersionsMetadata
import io.github.recrafter.crafter.core.extensions.getRunTaskName
import io.github.recrafter.crafter.core.extensions.supportedVersionRange
import io.github.recrafter.crafter.core.helpers.MixinsHelper
import io.github.recrafter.crafter.core.helpers.server.EulaHelper
import io.github.recrafter.crafter.core.helpers.server.ServerOperatorsHelper
import io.github.recrafter.crafter.core.helpers.server.ServerPropertiesHelper
import io.github.recrafter.crafter.core.sync.parchment.ParchmentSynchronizer
import io.github.recrafter.crafter.core.tasks.ZipSplitSidesModTask
import io.github.recrafter.crafter.core.tasks.test.CraftClientTask
import io.github.recrafter.crafter.core.tasks.test.CraftServerTask
import io.github.recrafter.crafter.extensions.configure
import io.github.recrafter.crafter.extensions.configureJvmTarget
import io.github.recrafter.crafter.extensions.gradle.CrafterExtension
import io.github.recrafter.crafter.extensions.kotlin
import io.github.recrafter.crafter.extensions.kotlinApply
import io.github.recrafter.crafter.extensions.mappers.mapToModel
import io.github.recrafter.crafter.extensions.mappers.toJvmTarget
import io.github.recrafter.crafter.fabric.extensions.loomRemapJar
import io.github.recrafter.crafter.fabric.sync.FabricLoaderSynchronizer
import io.github.recrafter.crafter.fabric.sync.FabricMappingsSynchronizer
import io.github.recrafter.crafter.forge.sync.ForgeLoaderSynchronizer
import io.github.recrafter.crafter.legacy_fabric.sync.LegacyFabricMappingsSynchronizer
import io.github.recrafter.crafter.neoforge.sync.NeoForgeLoaderSynchronizer
import io.github.recrafter.crafter.ornithe.sync.OrnitheMappingsSynchronizer
import io.github.recrafter.crafter.quilt.sync.QuiltLoaderSynchronizer
import io.github.recrafter.crafter.quilt.sync.QuiltMappingsSynchronizer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import java.io.File

@Suppress("unused")
class CrafterGradlePlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        if (!project.isRootProject()) {
            gradleError("The Crafter must be applied only to the root project")
        }

        val extension = registerExtension<CrafterExtension>()
        extension.onConfigurationReady {
            val modMetadata = extension.buildModMetadata(getModRecipe())
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
        val loader = loaderType.mapToModel()
        val versionProjects = loaderProject.children
        val projectVersions = versionProjects.associateWith { MinecraftVersion.parse(it.name) }
        projectVersions.forEach { (versionProject, minMinecraftVersion) ->
            val maxMinecraftVersion = projectVersions.values
                .filter { it > minMinecraftVersion }
                .minWithOrNull(MinecraftVersion.COMPARATOR)
                ?.previousOrNull()
                ?: loaderType.supportedVersionRange.max
            val versionsMetadata = when (loaderType) {
                ModLoaderType.FABRIC -> VersionsMetadata(
                    loader = FabricLoaderSynchronizer.getLatestVersion(loaderProject, minMinecraftVersion),
                    mappings = FabricMappingsSynchronizer.getLatestVersion(loaderProject, minMinecraftVersion),
                )

                ModLoaderType.QUILT -> VersionsMetadata(
                    loader = QuiltLoaderSynchronizer.getLatestVersion(loaderProject, minMinecraftVersion),
                    mappings = QuiltMappingsSynchronizer.getLatestVersion(loaderProject, minMinecraftVersion),
                )

                ModLoaderType.LEGACY_FABRIC -> VersionsMetadata(
                    loader = FabricLoaderSynchronizer.getLatestVersion(loaderProject, minMinecraftVersion),
                    mappings = LegacyFabricMappingsSynchronizer.getLatestVersion(
                        loaderProject,
                        minMinecraftVersion
                    ),
                    mappingsMinecraft = LegacyFabricMappingsSynchronizer.getLatestComponent(
                        loaderProject,
                        minMinecraftVersion
                    ).minecraftVersion.asString(),
                )

                ModLoaderType.ORNITHE -> VersionsMetadata(
                    loader = FabricLoaderSynchronizer.getLatestVersion(loaderProject, minMinecraftVersion),
                    mappings = OrnitheMappingsSynchronizer(minMinecraftVersion.mappingsType).getLatestVersion(
                        loaderProject,
                        minMinecraftVersion
                    ),
                )

                ModLoaderType.BABRIC -> VersionsMetadata(
                    loader = BabricLoaderSynchronizer.getLatestVersion(loaderProject, minMinecraftVersion),
                    mappings = BabricMappingsSynchronizer.getLatestVersion(loaderProject, minMinecraftVersion),
                )

                ModLoaderType.FORGE -> VersionsMetadata(
                    loader = ForgeLoaderSynchronizer.getLatestVersion(loaderProject, minMinecraftVersion),
                    mappings = ParchmentSynchronizer.getLatestVersion(loaderProject, minMinecraftVersion),
                    mappingsMinecraft = ParchmentSynchronizer.getLatestComponent(
                        loaderProject,
                        minMinecraftVersion
                    ).minecraftVersion.asString(),
                )

                ModLoaderType.NEOFORGE -> VersionsMetadata(
                    loader = NeoForgeLoaderSynchronizer.getLatestVersion(loaderProject, minMinecraftVersion),
                    mappings = ParchmentSynchronizer.getLatestVersion(loaderProject, minMinecraftVersion),
                    mappingsMinecraft = ParchmentSynchronizer.getLatestComponent(
                        loaderProject,
                        minMinecraftVersion
                    ).minecraftVersion.asString(),
                )
            }
            val mod = modMetadata.toMod(loaderType, minMinecraftVersion, maxMinecraftVersion, versionsMetadata)
            val archiveVersion = mod.archiveVersion(loaderType, minMinecraftVersion, maxMinecraftVersion)
            val iconFile = loaderProject.projectDirectory.parentFile?.resolve(mod.iconFileName)?.asFileOrNull()
            configureVersionProject(mod, loader, iconFile, versionProject, archiveVersion)
        }
    }

    private fun configureVersionProject(
        mod: Mod,
        loader: ModLoader,
        iconFile: File?,
        project: Project,
        archiveVersion: String,
    ) {
        with(project) {
            ensurePluginApplied("org.jetbrains.kotlin.jvm")
            group = mod.namespace
            version = archiveVersion
            base {
                archivesName = mod.id
            }
            java {
                withSourcesJar()
                toolchain {
                    configureJavaVendor(mod.javaVersion, JvmVendorSpec.ADOPTIUM, JvmVendorSpec.AZUL)
                }
            }
            kotlin {
                jvmToolchain(mod.javaVersion)
            }
            tasks {
                configureJvmTarget(mod.minJavaVersion.toJvmTarget())
                withType<JavaCompile>().configureEach {
                    options.encoding = Charsets.UTF_8.toString()
                }
                jar {
                    from("LICENSE") {
                        rename { it + Constants.Char.UNDERSCORE + mod.id }
                    }
                    manifest {
                        val specificationVersion by 1.toString().autoNamedProperty(`Train-Case`)
                        val specificationTitle by mod.id.autoNamedProperty(`Train-Case`)
                        val specificationVendor by mod.developer.autoNamedProperty(`Train-Case`)

                        val implementationVersion by archiveVersion.autoNamedProperty(`Train-Case`)
                        val implementationTitle by mod.id.autoNamedProperty(`Train-Case`)
                        val implementationVendor by mod.developer.autoNamedProperty(`Train-Case`)

                        attributes(
                            listOf(
                                specificationVersion,
                                specificationTitle,
                                specificationVendor,

                                implementationVersion,
                                implementationTitle,
                                implementationVendor,
                            ).associate { it.name to it.value }
                        )
                    }
                    this.archiveVersion = archiveVersion
                }
            }
        }
        val isMergedMappings = mod.minecraftVersion.mappingsType == MappingsType.MERGED
        val sideProjects = mod.environment.sides.associateWith { side ->
            project.children.first { it.name == side.getName() }.kotlinApply {
                ensurePluginApplied("org.jetbrains.kotlin.jvm")
            }
        }
        project.projectDirectory.resolve(mod.runDirectoryName).resolve(ModSide.SERVER.getName()).ensureDirectoryExists {
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
            val pluginProject = if (isMergedMappings) project else sideProject
            val pluginMainSourceSet = pluginProject.sourceSets.main
            if (isMergedMappings) {
                sideProject.sourceSets.main.addToClasspath(pluginMainSourceSet, withOutput = true)
            }
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
        if (isMergedMappings) {
            loader.configure(mod, iconFile, project, sideProjects)
        } else {
            val splitSideJarTasks = mutableListOf<TaskProvider<out Jar>>()
            val libsDirectory = project.getBuildDirectory("libs")
            val isFabricFamily = mod.loaderFamily == ModLoaderFamily.FABRIC
            sideProjects.forEach { (side, sideProject) ->
                with(project) {
                    tasks {
                        build {
                            dependsOn(sideProject.tasks.build)
                        }
                    }
                }
                loader.configure(mod, iconFile, sideProject, mapOf(side to sideProject))
                val sideJarTask = when {
                    isFabricFamily -> sideProject.tasks.loomRemapJar
                    else -> sideProject.tasks.jar
                }
                with(sideProject) {
                    tasks {
                        val versionJarTask = project.tasks.jar.get()
                        jar {
                            destinationDirectory = when {
                                isFabricFamily -> project.getBuildDirectory("devlibs")
                                else -> libsDirectory
                            }
                            val jarClassifier = buildString {
                                append(side.getName())
                                archiveClassifier.orNull?.let { append(Constants.Char.HYPHEN + it) }
                            }
                            copyArchiveName(versionJarTask, classifier = jarClassifier)
                        }
                        if (isFabricFamily) {
                            sideJarTask {
                                destinationDirectory = libsDirectory
                                copyArchiveName(versionJarTask, classifier = side.getName())
                            }
                        }
                    }
                }
                if (mod.environment == ModEnvironment.CLIENT_SERVER) {
                    splitSideJarTasks.add(sideJarTask)
                } else {
                    with(project) {
                        val splitSideJar = sideJarTask.get()
                        tasks {
                            jar {
                                copyArchiveName(splitSideJar)
                                disable()
                            }
                        }
                        artifacts {
                            add("archives", splitSideJar.archiveFile) {
                                builtBy(splitSideJar)
                            }
                        }
                    }
                }
            }
            if (splitSideJarTasks.isNotEmpty()) {
                project.registerTask<ZipSplitSidesModTask> {
                    dependsOn(splitSideJarTasks)
                    val sideJarTask = splitSideJarTasks.first().get()
                    from(splitSideJarTasks.map { task -> task.map { jar -> jar.archiveFile.get().asFile } })
                    destinationDirectory = sideJarTask.destinationDirectory
                    copyArchiveName(sideJarTask, classifier = null, extension = Constants.File.Extension.ZIP)
                }
            }
        }
        ModSide.values().forEach { side ->
            val pluginProject = if (isMergedMappings) project else sideProjects.values.first()
            when (side) {
                ModSide.CLIENT -> project.registerTask<CraftClientTask>()
                ModSide.SERVER -> project.registerTask<CraftServerTask>()
            } {
                dependsSequentiallyOn(
                    buildList {
                        add(project.tasks.build.get())
                        addAll(loader.getPrepareRunTasks(pluginProject, side))
                        addIfNotNull(pluginProject.getTaskOrNull(side.getRunTaskName()))
                    }
                )
            }
        }
    }
}
