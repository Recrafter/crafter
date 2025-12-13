package io.github.recrafter.crafter.core

import io.github.diskria.gradle.utils.extensions.*
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.DateFormat
import io.github.diskria.kotlin.utils.extensions.asFileOrNull
import io.github.diskria.kotlin.utils.extensions.common.`Train-Case`
import io.github.diskria.kotlin.utils.extensions.common.nowDate
import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.format
import io.github.diskria.kotlin.utils.extensions.walkFilesWithExtension
import io.github.diskria.kotlin.utils.properties.autoNamedProperty
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.minJavaVersion
import io.github.recrafter.crafter.core.extensions.*
import io.github.recrafter.crafter.tasks.internal.CraftDataPackConfigTask
import io.github.recrafter.crafter.tasks.internal.CraftEntryPointsTask
import io.github.recrafter.crafter.tasks.internal.CraftMixinsConfigTask
import io.github.recrafter.crafter.tasks.internal.CraftWidenerConfigTask
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.*
import java.io.File

abstract class ModLoaderAdapter {

    open fun getPrepareRunTasks(loaderProject: Project, side: ModSide): List<Task> = emptyList()
    open fun isDataPackConfigRequired(): Boolean = false
    open fun shouldDownloadSources(): Boolean = false

    abstract fun configurePlugin(mod: Mod, project: Project, runDirectory: File, widenerConfig: File)

    fun configureInternal(
        mod: Mod,
        modProject: Project,
        runDirectory: File,
        sideProjects: Map<ModSide, Project>,
    ) = with(modProject) {
        group = mod.namespace
        base {
            archivesName = mod.id
        }
        java {
            sourceCompatibility = JavaVersion.toVersion(mod.javaVersion)
            targetCompatibility = JavaVersion.toVersion(mod.jvmTarget.toInt())
            withSourcesJar()
            toolchain {
                configureJavaVendor(mod.javaVersion, JvmVendorSpec.ADOPTIUM, JvmVendorSpec.AZUL)
            }
        }
        kotlin {
            jvmToolchain(mod.javaVersion)
        }
        tasks {
            configureJvmTarget(mod.jvmTarget)
            withType<JavaCompile>().configureEach {
                with(options) {
                    encoding = Charsets.UTF_8.toString()
                    compilerArgs.add("-Xlint:-options")
                }
            }
            lazyConfigure<Jar>("sourcesJar") {
                exclude(mod.widenerConfigPath)
            }
            jar {
                from("LICENSE") {
                    rename { it + Constants.Char.UNDERSCORE + mod.id }
                }
                manifest {
                    val specificationVersion by 1.toString().autoNamedProperty(`Train-Case`)
                    val specificationTitle by mod.id.autoNamedProperty(`Train-Case`)
                    val specificationVendor by mod.developer.autoNamedProperty(`Train-Case`)

                    val implementationVersion by mod.archiveVersion.autoNamedProperty(`Train-Case`)
                    val implementationTitle by mod.id.autoNamedProperty(`Train-Case`)
                    val implementationVendor by mod.developer.autoNamedProperty(`Train-Case`)
                    val implementationTimestamp by nowDate().format(DateFormat.ISO_DATE_TIME)
                        .autoNamedProperty(`Train-Case`)

                    val crafterVersion by mod.pluginVersion.autoNamedProperty(`Train-Case`)

                    attributes(
                        listOf(
                            specificationVersion,
                            specificationTitle,
                            specificationVendor,

                            implementationVersion,
                            implementationTitle,
                            implementationVendor,
                            implementationTimestamp,

                            crafterVersion,
                        ).associate { it.name to it.value }
                    )
                }
                archiveVersion = mod.archiveVersion
            }
        }
        idea {
            module {
                if (shouldDownloadSources()) {
                    isDownloadSources = true
                    isDownloadJavadoc = true
                }
            }
        }
        val classesToWiden = sideProjects.flatMap { (_, sideProject) ->
            sideProject.sourceSets.accessors.java.srcDirs.flatMap { srcDir ->
                srcDir.walkFilesWithExtension(extension = Constants.File.Extension.KOTLIN).flatMap { kotlinClass ->
                    CraftWidenerConfigTask.extractClassNames(kotlinClass)
                }
            }
        }.toList()
        val widenerConfig = craftedResourcesDirectory.resolve(mod.widenerConfigPath).ensureFileExists().apply {
            writeText(buildString {
                mod.loader.widenerConfigFormat.header?.let { appendLine(it) }
                classesToWiden.forEach { className ->
                    appendLine(mod.loader.widenerConfigFormat.entryOf(className))
                }
            })
        }
        tasks {
            withType<AbstractCopyTask> {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }
            ModSide.values().forEach { side ->
                lazyConfigure<JavaExec>(side.getRunTaskName()) {
                    if (mod.loader == ModLoaderType.NEOFORGE) {
                        addToClasspath(jar.get().archiveFile)
                    }
                    if (mod.loader != ModLoaderType.BABRIC) {
                        javaLauncher = modProject.getExtension<JavaToolchainService>().launcherFor {
                            configureJavaVendor(
                                mod.minecraftVersion.minJavaVersion,
                                JvmVendorSpec.ADOPTIUM,
                                JvmVendorSpec.AZUL,
                            )
                        }
                    }
                }
            }
            if (isDataPackConfigRequired()) {
                val craftDataPackConfigTask = registerTask<CraftDataPackConfigTask> {
                    this.mod.set(mod)
                    minFormat = mod.minMinecraftVersion.getDataPackFormat(modProject)
                    maxFormat = mod.maxMinecraftVersion.getDataPackFormat(modProject)
                    outputFile = getTempFile(mod.resourcePackConfigName)
                }
                processResources {
                    copyTaskOutput(craftDataPackConfigTask)
                }
            }
            val craftMixinsConfigTask = registerTask<CraftMixinsConfigTask> {
                dependsOn(modProject.tasks.named("kspKotlin"))
                this.mod.set(mod)
                mixinSourceSetDirectories = sideProjects.mapValues {
                    it.value.sourceSets.mixins.java.srcDirs + it.value.sourceSets.accessors.java.srcDirs
                }
                configureInputFiles()
                outputFile.set(getTempFile(mod.mixinsConfigName))
            }
            processResources {
                copyTaskOutput(craftMixinsConfigTask, mod.mixinsConfigPath)
                modProject.rootProject.projectDirectory.resolve(mod.iconFileName).asFileOrNull()?.let { iconFile ->
                    copyFile(iconFile, mod.iconPath)
                }
                copyFile(widenerConfig, mod.widenerConfigPath)
            }
        }
        val craftEntryPointsTask = registerTask<CraftEntryPointsTask> {
            this.mod.set(mod)
            sides = sideProjects.keys
            outputDirectory = craftedSourcesDirectory
        }
        sourceSets {
            with(main) {
                val mergedSourceSetsDirectory = getBuildDirectory("sources+resources")
                val sideSourceSets = sideProjects.flatMap { it.value.sourceSets }
                java {
                    srcDirs(
                        craftEntryPointsTask.map { it.outputDirectory },
                        sideSourceSets.flatMap { it.java.srcDirs },
                    )
                    destinationDirectory = mergedSourceSetsDirectory
                }
                resources {
                    srcDirs(
                        craftedResourcesDirectory,
                        sideSourceSets.flatMap { it.resources.srcDirs },
                    )
                }
                output.setResourcesDir(mergedSourceSetsDirectory)
            }
        }
        configurePlugin(mod, modProject, runDirectory, widenerConfig)
        groupIdeTasks()
        restoreDependencyResolutionRepositories()
        repositories {
            mavenLocal()
        }
        dependencies {
            val nametag = compileOnly(ksp("io.github.recrafter", "nametag", "0.2.1"))
            mod.log(modProject, "Nametag: $nametag")
        }
    }
}
