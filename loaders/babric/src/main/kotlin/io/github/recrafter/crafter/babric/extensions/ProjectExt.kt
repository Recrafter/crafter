package io.github.recrafter.crafter.babric.extensions

import io.github.diskria.gradle.utils.extensions.withPluginExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project

fun Project.babric(configure: LoomGradleExtensionAPI.() -> Unit = {}) {
    withPluginExtension<LoomGradleExtensionAPI>("babric-loom", configure)
}
