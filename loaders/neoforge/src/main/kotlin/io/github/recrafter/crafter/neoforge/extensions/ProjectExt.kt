package io.github.recrafter.crafter.neoforge.extensions

import io.github.diskria.gradle.utils.extensions.*
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Project

fun Project.neoforge(configure: NeoForgeExtension.() -> Unit = {}) {
    withPluginExtension<NeoForgeExtension>("net.neoforged.moddev", configure)
}
