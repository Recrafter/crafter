package io.github.recrafter.crafter.loaders.neoforge.extensions

import io.github.diskria.gradle.utils.extensions.withPluginExtension
import net.neoforged.moddevgradle.dsl.ModDevExtension
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Project

fun Project.neoforge(configure: NeoForgeExtension.() -> Unit = {}) {
    withPluginExtension<NeoForgeExtension>("net.neoforged.moddev", configure)
}

fun Project.moddev(configure: ModDevExtension.() -> Unit = {}) {
    withPluginExtension<ModDevExtension>("net.neoforged.moddev", configure)
}
