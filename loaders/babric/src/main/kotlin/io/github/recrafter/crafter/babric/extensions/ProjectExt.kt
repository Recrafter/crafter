package io.github.recrafter.crafter.babric.extensions

import babric.BabricExtension
import io.github.diskria.gradle.utils.extensions.ensurePluginApplied
import io.github.diskria.gradle.utils.extensions.getExtension
import org.gradle.api.Project

val Project.babric: BabricExtension
    get() {
        ensurePluginApplied("maven-publish")
        ensurePluginApplied("babric-loom-extension")
        return getExtension<BabricExtension>()
    }

fun Project.babric(configure: BabricExtension.() -> Unit = {}) {
    babric.apply(configure)
}
