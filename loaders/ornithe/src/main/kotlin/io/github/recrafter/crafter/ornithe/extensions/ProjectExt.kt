package io.github.recrafter.crafter.ornithe.extensions

import io.github.diskria.gradle.utils.extensions.ensurePluginApplied
import io.github.diskria.gradle.utils.extensions.getExtension
import net.ornithemc.ploceus.api.PloceusGradleExtensionApi
import org.gradle.api.Project

val Project.ornithe: PloceusGradleExtensionApi
    get() {
        ensurePluginApplied("ploceus")
        return getExtension<PloceusGradleExtensionApi>()
    }

fun Project.ornithe(configure: PloceusGradleExtensionApi.() -> Unit = {}) {
    ornithe.apply(configure)
}
