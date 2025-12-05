package io.github.recrafter.crafter.loaders.legacy_fabric.extensions

import io.github.diskria.gradle.utils.extensions.ensurePluginApplied
import io.github.diskria.gradle.utils.extensions.getExtension
import net.legacyfabric.legacylooming.LegacyUtilsExtension
import org.gradle.api.Project

val Project.legacyFabric: LegacyUtilsExtension
    get() {
        ensurePluginApplied("legacy-looming")
        return getExtension<LegacyUtilsExtension>()
    }

fun Project.legacyFabric(configure: LegacyUtilsExtension.() -> Unit = {}) {
    legacyFabric.apply(configure)
}
