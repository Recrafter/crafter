package io.github.recrafter.crafter.core

import io.github.diskria.gradle.utils.extensions.*
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.DateFormat
import io.github.diskria.kotlin.utils.extensions.common.`Train-Case`
import io.github.diskria.kotlin.utils.extensions.common.nowDate
import io.github.diskria.kotlin.utils.extensions.format
import io.github.diskria.kotlin.utils.properties.autoNamedProperty
import io.github.recrafter.bedrock.era.FullRelease
import io.github.recrafter.bedrock.loaders.ModLoaderFamily
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.compareTo
import io.github.recrafter.bedrock.versions.minJavaVersion
import io.github.recrafter.crafter.core.extensions.*
import io.github.recrafter.crafter.tasks.internal.CraftDataPackConfigTask
import io.github.recrafter.crafter.tasks.internal.CraftEntryPointsTask
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

abstract class ModLoaderAdapter {

    open fun isDataPackConfigRequired(): Boolean = false

    abstract fun configurePlugin(mod: Mod, project: Project, runDirectory: File, accessorConfig: File)

    open fun getGameJars(project: Project): FileCollection = project.files()

    fun configureInternal(
        mod: Mod,
        modProject: Project,
        runDirectory: File,
        accessorConfig: File,
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
            if (accessorConfig.isFile) {
                lazyConfigure<Jar>("sourcesJar") {
                    exclude(mod.accessorConfigPath)
                }
            }
            withType<Jar> {
                from(rootProject.file("LICENSE")) {
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
                excludeFilesWithExtension("kotlin_module")
            }
            withType<AbstractCopyTask> {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }
            ModSide.entries.forEach { side ->
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
            processResources {
                moveFile(mod.mixinConfigName, mod.mixinConfigPath)
                copyFile(accessorConfig, mod.accessorConfigPath)
                modProject.rootProject.projectDirectory.resolve(mod.iconFileName).let { copyFile(it, mod.iconPath) }
            }
            register("lapis") {
                group = CrafterTasks.PUBLIC_GROUP
                dependsOn("kspKotlin")
            }
            withType<JavaCompile>().configureEach {
                with(options) {
                    encoding = Charsets.UTF_8.toString()
                    compilerArgs.add("-Xlint:-options")
                }
                mustRunAfter(processResources)
            }
            withType<KotlinCompile>().configureEach {
                mustRunAfter(processResources)
            }
        }
        val craftEntryPointsTask = registerTask<CraftEntryPointsTask> {
            this.mod.set(mod)
            sides = sideProjects.keys
            outputDirectory = craftedSourcesDirectory
        }
        sourceSets {
            with(main) {
                val mergedDirectory = getBuildDirectory("sources+resources")
                val sideSourceSets = sideProjects.map { it.value.sourceSets.main }
                kotlin.srcDir(getGeneratedDirectory().resolve("ksp/main/kotlin"))
                sideSourceSets.forEach { kotlin.srcDirs(it.kotlin.srcDirs) }
                kotlin.destinationDirectory.set(mergedDirectory)
                java {
                    srcDir(craftEntryPointsTask.map { it.outputDirectory })
                    sideSourceSets.forEach { srcDirs(it.allJava.srcDirs) }
                    destinationDirectory.set(mergedDirectory)
                }
                resources {
                    srcDir(craftedResourcesDirectory)
                    sideSourceSets.forEach { srcDirs(it.resources.srcDirs) }
                    destinationDirectory.set(mergedDirectory)
                }
                output.setResourcesDir(mergedDirectory)
                (output.classesDirs as ConfigurableFileCollection).setFrom(mergedDirectory)
            }
        }
        configurePlugin(mod, modProject, runDirectory, accessorConfig)
        ksp {
            arg("lapis.modId", mod.id)
            arg("lapis.packageName", mod.packageName)
            arg("lapis.isUnobfuscated", (mod.minecraftVersion >= FullRelease.V_26_1).toString())
            arg(
                when (mod.loader.family) {
                    ModLoaderFamily.FABRIC -> "lapis.accessWidenerConfigName"
                    ModLoaderFamily.FORGE -> "lapis.accessTransformerConfigName"
                },
                accessorConfig.name
            )
        }
        groupIdeTasks()
    }
}
