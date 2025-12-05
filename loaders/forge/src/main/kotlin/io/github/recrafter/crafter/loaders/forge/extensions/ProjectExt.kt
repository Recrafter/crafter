package io.github.recrafter.crafter.loaders.forge.extensions

import io.github.diskria.gradle.utils.extensions.withPluginExtension
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.gradle.api.Project

fun Project.forge(configure: UserDevExtension.() -> Unit = {}) {
    withPluginExtension<UserDevExtension>("net.minecraftforge.gradle", configure)
}
