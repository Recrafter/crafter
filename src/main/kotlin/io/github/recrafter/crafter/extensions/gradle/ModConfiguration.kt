package io.github.recrafter.crafter.extensions.gradle

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.kotlin.utils.extensions.common.`Title Case`
import io.github.diskria.kotlin.utils.extensions.common.snake_case
import io.github.diskria.kotlin.utils.extensions.setCase
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class ModConfiguration(protected val objects: ObjectFactory) {

    val name: Property<String> = objects.property()
    val id: Property<String> = objects.property<String>().convention(name.map { it.setCase(`Title Case`, snake_case) })
    val description: Property<String> = objects.property<String>().convention("Minecraft Mod")
    val version: Property<String> = objects.property()
    val licenseId = objects.property<String>().convention("MIT")
    val homepageUrl: Property<String> = objects.property()
    val runDirectoryName: Property<String> = objects.property<String>().convention("run")
    val javaVersion: Property<Int> = objects.property<Int>().convention(21)

    internal val developer: DeveloperConfiguration
        get() = developerConfiguration ?: gradleError("Developer configuration missing")

    private var developerConfiguration: DeveloperConfiguration? = null

    fun developer(configure: DeveloperConfiguration.() -> Unit) {
        developerConfiguration = DeveloperConfiguration(objects).apply(configure)
    }
}
