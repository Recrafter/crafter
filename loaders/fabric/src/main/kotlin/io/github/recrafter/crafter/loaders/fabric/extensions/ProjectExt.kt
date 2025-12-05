package io.github.recrafter.crafter.loaders.fabric.extensions

import io.github.diskria.gradle.utils.extensions.ensurePluginApplied
import io.github.diskria.gradle.utils.extensions.getExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project

val Project.quilt: LoomGradleExtensionAPI
    get() {
        ensurePluginApplied("org.quiltmc.loom")
        return getExtension<LoomGradleExtensionAPI>()
    }

fun Project.quilt(configure: LoomGradleExtensionAPI.() -> Unit = {}) {
    quilt.apply(configure)
}
