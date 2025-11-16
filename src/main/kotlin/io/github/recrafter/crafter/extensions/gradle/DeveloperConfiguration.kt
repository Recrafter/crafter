package io.github.recrafter.crafter.extensions.gradle

import io.github.recrafter.bedrock.MinecraftConstants
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class DeveloperConfiguration(objects: ObjectFactory) {
    val name: Property<String> = objects.property()
    val namespace: Property<String> = objects.property()
    val testPlayerName: Property<String> = objects.property<String>()
        .convention(name.map { it + MinecraftConstants.DEVELOPER_USERNAME_SUFFIX })
    val repoUrl: Property<String> = objects.property()
    val issuesUrl: Property<String> = objects.property()
}
