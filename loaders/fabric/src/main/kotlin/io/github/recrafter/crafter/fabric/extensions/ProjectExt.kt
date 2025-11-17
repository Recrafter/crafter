package io.github.recrafter.crafter.fabric.extensions

import io.github.diskria.gradle.utils.extensions.ensurePluginApplied
import io.github.diskria.gradle.utils.extensions.getExtension
import io.github.diskria.gradle.utils.extensions.withPluginExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.legacyfabric.legacylooming.LegacyUtilsExtension
import net.ornithemc.ploceus.api.PloceusGradleExtensionApi
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named

val TaskContainer.loomRemapJar: TaskProvider<Jar>
    get() = named<Jar>("remapJar")

val Project.legacyFabric: LegacyUtilsExtension
    get() {
        ensurePluginApplied("legacy-looming")
        return getExtension<LegacyUtilsExtension>()
    }

val Project.ornithe: PloceusGradleExtensionApi
    get() {
        ensurePluginApplied("ploceus")
        return getExtension<PloceusGradleExtensionApi>()
    }

fun Project.fabric(configure: LoomGradleExtensionAPI.() -> Unit = {}) {
    withPluginExtension<LoomGradleExtensionAPI>("fabric-loom", configure)
}

fun Project.legacyFabric(configure: LegacyUtilsExtension.() -> Unit = {}) {
    legacyFabric.apply(configure)
}

fun Project.ornithe(configure: PloceusGradleExtensionApi.() -> Unit = {}) {
    ornithe.apply(configure)
}
