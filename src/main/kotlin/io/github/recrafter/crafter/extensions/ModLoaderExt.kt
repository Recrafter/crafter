package io.github.recrafter.crafter.extensions

import io.github.diskria.gradle.utils.extensions.*
import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MappingsType
import io.github.recrafter.bedrock.versions.mappingsType
import io.github.recrafter.bedrock.versions.minJavaVersion
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.ModLoader
import io.github.recrafter.crafter.core.extensions.getDataPackFormat
import io.github.recrafter.crafter.core.extensions.getRunTaskName
import io.github.recrafter.crafter.core.extensions.mixins
import io.github.recrafter.crafter.core.helpers.AccessConfigHelper
import io.github.recrafter.crafter.core.tasks.GenerateDataPackConfigTask
import io.github.recrafter.crafter.core.tasks.GenerateModEntryPointsTask
import io.github.recrafter.crafter.core.tasks.GenerateModMixinsConfigTask
import io.github.recrafter.crafter.tasks.generate.GenerateModConfigTask
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import java.io.File

fun ModLoader.configure(
    mod: Mod,
    iconFile: File?,
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
    tasks {
        if (!isMergedMappings) {
            withType<AbstractCopyTask> {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }
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
        if (isDataPackConfigRequired()) {
            val generateDataPackConfigTask = registerTask<GenerateDataPackConfigTask> {
                this.mod = mod
                minFormat = mod.minMinecraftVersion.getDataPackFormat(pluginProject)
                maxFormat = mod.maxMinecraftVersion.getDataPackFormat(pluginProject)
                outputFile = getTempFile(mod.resourcePackConfigName)
            }
            processResources {
                copyTaskOutput(generateDataPackConfigTask)
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
            iconFile?.let { copyFile(it, mod.assetsPath) }
            if (!isMergedMappings) {
                moveFile(mod.accessConfigName, mod.accessConfigPath)
            }
        }
    }
    val generateModEntryPointsTask = registerTask<GenerateModEntryPointsTask> {
        this.mod = mod
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
