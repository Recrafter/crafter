package io.github.recrafter.crafter.extensions

import io.github.diskria.gradle.utils.extensions.*
import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MappingsType
import io.github.recrafter.bedrock.versions.mappingsType
import io.github.recrafter.bedrock.versions.minJavaVersion
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.ModLoaderAdapter
import io.github.recrafter.crafter.core.extensions.getDataPackFormat
import io.github.recrafter.crafter.core.extensions.getRunTaskName
import io.github.recrafter.crafter.core.extensions.groupMatchingTasks
import io.github.recrafter.crafter.core.extensions.mixins
import io.github.recrafter.crafter.core.helpers.AccessConfigHelper
import io.github.recrafter.crafter.core.tasks.craft.internal.CraftDataPackConfigTask
import io.github.recrafter.crafter.core.tasks.craft.internal.CraftEntryPointsTask
import io.github.recrafter.crafter.core.tasks.craft.internal.CraftMixinsConfigTask
import io.github.recrafter.crafter.tasks.craft.internal.CraftModConfigTask
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

fun ModLoaderAdapter.configure(
    mod: Mod,
    iconFile: File?,
    versionProject: Project,
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
                if (side == ModSide.CLIENT) {
                    javaLauncher = pluginProject.getExtension<JavaToolchainService>().launcherFor {
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
                this.mod = mod
                minFormat = mod.minMinecraftVersion.getDataPackFormat(pluginProject)
                maxFormat = mod.maxMinecraftVersion.getDataPackFormat(pluginProject)
                outputFile = getTempFile(mod.resourcePackConfigName)
            }
            processResources {
                copyTaskOutput(craftDataPackConfigTask)
            }
        }
        val craftMixinsConfigTask = registerTask<CraftMixinsConfigTask> {
            this.mod = mod
            sideSourceSetDirectories = sideProjects.mapValues { it.value.sourceSets.mixins.java.srcDirs.first() }
            outputFile = getTempFile(mod.mixinsConfigName)
        }
        val craftModConfigTask = registerTask<CraftModConfigTask> {
            this.mod = mod
            this.splitSide = splitSide
            outputFile = getTempFile(mod.configName)
        }
        processResources {
            copyTaskOutput(craftMixinsConfigTask, mod.assetsPath)
            copyTaskOutput(craftModConfigTask, mod.configParentPath)
            iconFile?.let { copyFile(it, mod.assetsPath) }
            if (!isMergedMappings) {
                moveFile(mod.accessConfigName, mod.accessConfigPath)
            }
        }
    }
    val craftEntryPointsTask = registerTask<CraftEntryPointsTask> {
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
                    craftEntryPointsTask.map { it.outputDirectory },
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
    configurePlugin(mod, versionProject, pluginProject, sideProjects.keys, accessConfig)
    groupIdeTasks()
}

private fun Project.groupIdeTasks() {
    groupMatchingTasks("IDE/Eclipse", "eclipse")
    groupMatchingTasks("IDE/VSCode", "vscode")
    groupMatchingTasks("IDE/IntelliJ IDEA", "intellij", "idea")
}
