package io.github.recrafter.crafter.loaders.forge.extensions

import io.github.diskria.gradle.utils.extensions.ensurePluginApplied
import io.github.diskria.gradle.utils.extensions.getExtension
import io.github.diskria.gradle.utils.extensions.withPluginExtension
import net.minecraftforge.gradle.ForgeGradleExtension
import net.minecraftforge.gradle.MinecraftExtensionForProject
import net.minecraftforge.jarjar.gradle.JarJarExtension
import org.gradle.api.Project

val Project.minecraftForge: MinecraftExtensionForProject
    get() {
        ensurePluginApplied("net.minecraftforge.gradle")
        return getExtension<MinecraftExtensionForProject>()
    }

val Project.forge: ForgeGradleExtension
    get() {
        ensurePluginApplied("net.minecraftforge.gradle")
        return getExtension<ForgeGradleExtension>()
    }

fun Project.minecraftForge(configure: MinecraftExtensionForProject.() -> Unit = {}) {
    minecraftForge.apply(configure)
}

fun Project.jarJar(configure: JarJarExtension.() -> Unit = {}) {
    withPluginExtension<JarJarExtension>("net.minecraftforge.jarjar", configure)
}
