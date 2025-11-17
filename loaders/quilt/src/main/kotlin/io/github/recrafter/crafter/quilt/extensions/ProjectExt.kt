package io.github.recrafter.crafter.quilt.extensions

import io.github.diskria.gradle.utils.extensions.withPluginExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project

fun Project.quilt(configure: LoomGradleExtensionAPI.() -> Unit = {}) {
    withPluginExtension<LoomGradleExtensionAPI>("org.quiltmc.loom", configure)
}
