package io.github.recrafter.crafter.core.extensions

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import com.google.devtools.ksp.gradle.KspExtension
import io.github.diskria.gradle.utils.extensions.configureExtension
import io.github.diskria.gradle.utils.extensions.ensurePluginApplied
import io.github.diskria.gradle.utils.extensions.getGeneratedResourcesDirectory
import io.github.diskria.gradle.utils.extensions.getGeneratedSourcesDirectory
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.mappers.toEnumOrNull
import io.github.recrafter.bedrock.crafter.CrafterConstants
import io.github.recrafter.bedrock.loaders.ModLoaderType
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

fun Project.ensureKotlinPluginApplied(): Project {
    ensurePluginApplied("org.jetbrains.kotlin.jvm")
    return this
}

fun Project.ensureKotlinSerializationPluginApplied(): Project {
    ensurePluginApplied("org.jetbrains.kotlin.plugin.serialization")
    return this
}

fun Project.ensureKspPluginApplied(): Project {
    ensurePluginApplied("com.google.devtools.ksp")
    return this
}

fun Project.isLoaderProject(): Boolean =
    name.toEnumOrNull<ModLoaderType>(`kebab-case`) != null

fun Project.groupLoaderTasks(
    loaderPackageNamePrefixes: List<String> = emptyList(),
    taskGroups: List<String> = emptyList(),
    taskNames: List<String> = emptyList(),
    loader: ModLoaderType,
) {
    gradle.taskGraph.whenReady {
        tasks {
            matching { task ->
                val packageName = task.javaClass.packageName.orEmpty()
                loaderPackageNamePrefixes.any { packageName.startsWith(it) } ||
                        task.group != null && taskGroups.contains(task.group) ||
                        taskNames.contains(task.name)
            }.configureEach {
                group = "Mod Loader".appendPath(loader.displayName)
            }
        }
    }
}

fun Project.groupMatchingTasks(name: String, vararg keywords: String) {
    gradle.taskGraph.whenReady {
        tasks {
            matching { task ->
                !task.isCrafterTask() &&
                        keywords.any { task.name.lowercase().contains(it.lowercase()) }
            }.configureEach {
                group = name
            }
        }
    }
}

val Project.craftedSourcesDirectory
    get() = getGeneratedSourcesDirectory().resolve(CrafterConstants.PLUGIN_LOWER_NAME)

val Project.craftedResourcesDirectory
    get() = getGeneratedResourcesDirectory().resolve(CrafterConstants.PLUGIN_LOWER_NAME)

fun Project.kotlin(configure: KotlinProjectExtension.() -> Unit = {}) {
    configureExtension<KotlinProjectExtension>(configure)
}

fun Project.ksp(configure: KspExtension.() -> Unit = {}) {
    configureExtension<KspExtension>(configure)
}

fun Project.idea(configure: IdeaModel.() -> Unit = {}) {
    ensurePluginApplied("idea")
    configureExtension<IdeaModel>(configure)
}

fun Project.shadowKotlin(
    relocateDestinationBase: String,
    needSerialization: Boolean = false,
    needCoroutines: Boolean = false,
) {
    val shadowImplementation = configurations.maybeCreate("shadowImplementation")
    configurations.getByName("implementation").extendsFrom(shadowImplementation)
    dependencies {
        add(shadowImplementation.name, "org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
        if (needSerialization) {
            add(shadowImplementation.name, "org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
        if (needCoroutines) {
            add(shadowImplementation.name, "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
    }
    ensurePluginApplied("com.gradleup.shadow")
    tasks.shadowJar {
        configurations = listOf(shadowImplementation)
        archiveClassifier = ""
        mergeServiceFiles()

        if (needCoroutines) {
            excludeDirectory("_COROUTINE")
            exclude(
                "DebugProbesKt.bin",
                "META-INF/kotlinx_coroutines_core.version",
            )
        }
        if (needSerialization || needCoroutines) {
            relocate("kotlinx", "$relocateDestinationBase.shadow.kotlinx")
            excludeDirectory(
                "META-INF/com.android.tools",
                "META-INF/proguard",
            )
        }
        relocate("kotlin", "$relocateDestinationBase.shadow.kotlin")
        excludeDirectory(
            "org/intellij/lang/annotations",
            "org/jetbrains/annotations",
            "META-INF/maven",
        )
    }
}

fun Project.groupIdeTasks() {
    groupMatchingTasks("IDE/Eclipse", "eclipse")
    groupMatchingTasks("IDE/VSCode", "vscode")
    groupMatchingTasks("IDE/IntelliJ IDEA", "intellij", "idea")
}
