package io.github.recrafter.crafter.fabric.extensions

import babric.BabricExtension
import io.github.diskria.gradle.utils.extensions.ensurePluginApplied
import io.github.diskria.gradle.utils.extensions.getExtension
import io.github.diskria.gradle.utils.extensions.withPluginExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.legacyfabric.legacylooming.LegacyUtilsExtension
import net.ornithemc.ploceus.api.PloceusGradleExtensionApi
import org.gradle.api.Project

val Project.legacyFabric: LegacyUtilsExtension
    get() {
        ensurePluginApplied("legacy-looming")
        return getExtension<LegacyUtilsExtension>()
    }

val Project.babric: BabricExtension
    get() {
        ensurePluginApplied("maven-publish")
        ensurePluginApplied("babric-loom-extension")
        return getExtension<BabricExtension>()
    }

val Project.ornithe: PloceusGradleExtensionApi
    get() {
        ensurePluginApplied("ploceus")
        return getExtension<PloceusGradleExtensionApi>()
    }

fun Project.loom(configure: LoomGradleExtensionAPI.() -> Unit = {}) {
    withPluginExtension<LoomGradleExtensionAPI>("org.quiltmc.loom", configure)
}

fun Project.legacyFabric(configure: LegacyUtilsExtension.() -> Unit = {}) {
    legacyFabric.apply(configure)
}

fun Project.babric(configure: BabricExtension.() -> Unit = {}) {
    babric.apply(configure)
}

fun Project.ornithe(configure: PloceusGradleExtensionApi.() -> Unit = {}) {
    ornithe.apply(configure)
}
