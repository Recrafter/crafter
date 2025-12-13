package io.github.recrafter.crafter.extensions.gradle

import io.github.diskria.gradle.utils.extensions.common.requireGradle
import io.github.diskria.gradle.utils.extensions.common.requireGradleNotNull
import io.github.diskria.gradle.utils.extensions.gradle.GradleExtension
import io.github.recrafter.bedrock.recipes.ModRecipe
import io.github.recrafter.crafter.CrafterGradlePlugin
import io.github.recrafter.crafter.core.ModMetadata
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class CrafterExtension @Inject constructor(protected val objects: ObjectFactory) : GradleExtension() {

    private var configuration: ModConfiguration? = null
    private var onConfigurationReadyCallback: (() -> Unit)? = null

    fun buildModMetadata(recipe: ModRecipe): ModMetadata {
        val configuration = requireConfiguration()
        return ModMetadata(
            id = requireProperty(configuration.id, configuration::id.name),
            name = requireProperty(configuration.name, configuration::name.name),
            description = requireProperty(configuration.description, configuration::description.name),
            version = requireProperty(configuration.version, configuration::version.name),
            licenseId = requireProperty(configuration.licenseId, configuration::licenseId.name),
            homepageUrl = configuration.homepageUrl.orNull,
            runDirectoryName = requireProperty(configuration.runDirectoryName, configuration::runDirectoryName.name),
            javaVersion = requireProperty(configuration.javaVersion, configuration::javaVersion.name),
            developer = requireProperty(configuration.developer.name, configuration.developer::name.name),
            player = requireProperty(
                configuration.developer.testPlayerName,
                configuration.developer::testPlayerName.name
            ),
            namespace = requireProperty(configuration.developer.namespace, configuration.developer::namespace.name),
            repoUrl = configuration.developer.repoUrl.orNull,
            issuesUrl = configuration.developer.issuesUrl.orNull,
            environment = recipe.environment,

            pluginVersion = requireGradleNotNull(CrafterGradlePlugin::class.java.`package`?.implementationVersion) {
                "Failed to get plugin version."
            }
        )
    }

    fun onConfigurationReady(callback: () -> Unit) {
        onConfigurationReadyCallback = callback
    }

    fun requireConfiguration(): ModConfiguration =
        requireGradleNotNull(configuration) { "Mod not configured." }

    fun mod(configure: ModConfiguration.() -> Unit = {}) {
        requireGradle(configuration == null) {
            "Mod already configured."
        }
        configuration = ModConfiguration(objects).apply(configure)
        onConfigurationReadyCallback?.invoke()
    }
}
