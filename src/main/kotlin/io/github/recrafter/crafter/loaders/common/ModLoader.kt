package io.github.recrafter.crafter.loaders.common

import io.github.diskria.gradle.utils.extensions.*
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MappingsType
import io.github.recrafter.bedrock.versions.mappingsType
import io.github.recrafter.bedrock.versions.minJavaVersion
import io.github.recrafter.crafter.extensions.*
import io.github.recrafter.crafter.helpers.AccessConfigHelper
import io.github.recrafter.crafter.models.Mod
import io.github.recrafter.crafter.tasks.generate.GenerateModConfigTask
import io.github.recrafter.crafter.tasks.generate.GenerateModEntryPointsTask
import io.github.recrafter.crafter.tasks.generate.GenerateModMixinsConfigTask
import io.github.recrafter.crafter.tasks.generate.GenerateResourcePackConfigTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import java.io.File

abstract class ModLoader {

    abstract fun configurePlugin(mod: Mod, project: Project, sides: Set<ModSide>, accessConfig: File): Any?

    fun configure(
        mod: Mod,
        modProject: Project,
        pluginProject: Project,
        sideProjects: Map<ModSide, Project>,
    ) = with(pluginProject) {
        val isMergedMappings = mod.minecraftVersion.mappingsType == MappingsType.MERGED
        val splitSide = if (isMergedMappings) null else sideProjects.keys.single()
        val sideAccessConfigs = sideProjects.mapValues { (_, sideProject) ->
            sideProject.sourceSets.main.resources.srcDirs.first().resolve(mod.accessConfigName).ensureFileExists {
                writeText(getAccessConfigPreset())
            }
        }
        val accessConfig = if (splitSide != null) {
            sideAccessConfigs.getValue(splitSide)
        } else {
            craftedResourcesDirectory.resolve(mod.accessConfigPath).ensureFileExists().apply {
                writeText(AccessConfigHelper.mergeConfigs(sideAccessConfigs.values))
            }
        }
        modProject.findCommonProject()?.let { commonProject ->
            dependencies {
                add("compileOnly", commonProject)
            }
            tasks {
                jar {
                    from(commonProject.sourceSets.main.output)
                }
            }
        }
        tasks {
            withType<AbstractCopyTask> {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }
            ModSide.values().forEach { side ->
                lazyConfigure<JavaExec>(side.getRunTaskName()) {
                    addToClasspath(jar.get().archiveFile)
                    if (isMergedMappings) {
                        javaLauncher = pluginProject.getExtension<JavaToolchainService>().launcherFor {
                            val javaVersion = mod.minecraftVersion.minJavaVersion
                            configureJavaVendor(javaVersion, JvmVendorSpec.ADOPTIUM, JvmVendorSpec.AZUL)
                        }
                    }
                }
            }
            if (isResourcePackConfigRequired()) {
                val generateResourcePackConfigTask = registerTask<GenerateResourcePackConfigTask> {
                    this.mod = mod
                    outputFile = getTempFile(mod.resourcePackConfigName)
                    format = mod.minecraftVersion.getResourcePackFormat(pluginProject)
                }
                processResources {
                    copyTaskOutput(generateResourcePackConfigTask)
                }
            }
            val generateMixinsConfigTask = registerTask<GenerateModMixinsConfigTask> {
                this.mod = mod
                sideSourceSetDirectories = sideProjects.mapValues { it.value.sourceSets.mixins.java.srcDirs.first() }
                outputFile = getTempFile(mod.mixinsConfigName)
            }
            val generateModConfigTask = registerTask<GenerateModConfigTask> {
                this.mod = mod
                this.splitSide = splitSide
                outputFile = getTempFile(mod.configName)
            }
            processResources {
                copyTaskOutput(generateMixinsConfigTask, mod.assetsPath)
                copyTaskOutput(generateModConfigTask, mod.configParentPath)
                copyFile(modProject.getFile(mod.iconFileName).asFile, mod.assetsPath)
                if (!isMergedMappings) {
                    moveFile(mod.accessConfigName, mod.accessConfigPath)
                }
            }
        }
        val generateModEntryPointsTask = registerTask<GenerateModEntryPointsTask> {
            minecraftMod = mod
            sides = sideProjects.keys
            outputDirectory = craftedSourcesDirectory
        }
        sourceSets {
            with(main) {
                val mergedSourceSetsDirectory = getBuildDirectory("sources+resources")
                val sideSourceSets = sideProjects.flatMap { it.value.sourceSets }
                java {
                    srcDirs(
                        generateModEntryPointsTask.map { it.outputDirectory },
                        sideSourceSets.flatMap { it.java.srcDirs },
                    )
                    destinationDirectory = mergedSourceSetsDirectory
                }
                resources {
                    srcDirs(sideSourceSets.flatMap { it.resources.srcDirs })
                    if (isMergedMappings) {
                        exclude(mod.accessConfigName)
                        srcDirs(craftedResourcesDirectory)
                    }
                }
                output.setResourcesDir(mergedSourceSetsDirectory)
            }
        }
        configurePlugin(mod, pluginProject, sideProjects.keys, accessConfig)
    }

    open fun getPrepareRunTasks(pluginProject: Project, side: ModSide): List<Task> = emptyList()

    open fun getAccessConfigPreset(): String = Constants.Char.EMPTY

    open fun isResourcePackConfigRequired(): Boolean = false
}
