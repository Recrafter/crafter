package io.github.recrafter.crafter.extensions

import io.github.diskria.gradle.utils.extensions.configureExtension
import io.github.diskria.gradle.utils.extensions.getGeneratedResourcesDirectory
import io.github.diskria.gradle.utils.extensions.getGeneratedSourcesDirectory
import io.github.recrafter.crafter.core.CrafterConstants
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

fun Project.kotlinApply(block: Project.() -> Unit): Project {
    block(this)
    return this
}

val Project.craftedSourcesDirectory
    get() = getGeneratedSourcesDirectory().resolve(CrafterConstants.PLUGIN_LOWER_NAME)

val Project.craftedResourcesDirectory
    get() = getGeneratedResourcesDirectory().resolve(CrafterConstants.PLUGIN_LOWER_NAME)


fun Project.kotlin(configure: KotlinProjectExtension.() -> Unit = {}) {
    configureExtension<KotlinProjectExtension>(configure)
}
