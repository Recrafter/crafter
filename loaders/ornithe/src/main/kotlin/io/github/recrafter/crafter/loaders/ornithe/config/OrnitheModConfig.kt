package io.github.recrafter.crafter.loaders.ornithe.config

import io.github.diskria.kotlin.utils.serialization.annotations.EncodeDefaults
import io.github.diskria.kotlin.utils.serialization.annotations.PrettyPrint
import io.github.recrafter.crafter.core.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@EncodeDefaults
@PrettyPrint
data class OrnitheModConfig(
    val schemaVersion: Int,
    val id: String,
    val version: String,
    val name: String,
    val description: String,

    @SerialName("authors")
    val developers: List<String>,

    @SerialName("license")
    val licenseId: String,

    @SerialName("icon")
    val iconPath: String,

    val environment: String,

    @SerialName("mixins")
    val mixinConfigPath: List<String>,

    @SerialName("accessWidener")
    val widenerConfigPath: String?,

    @SerialName("contact")
    val links: OrnitheModLinksConfig,

    @SerialName("entrypoints")
    val entryPoints: OrnitheModEntryPointsConfig,

    @SerialName("depends")
    val dependencies: OrnitheModDependenciesConfig,
) {
    companion object {
        fun of(mod: Mod, hasAccessor: Boolean): OrnitheModConfig =
            OrnitheModConfig(
                schemaVersion = 1,
                id = mod.id,
                version = mod.version,
                name = mod.name,
                description = mod.description,
                developers = listOf(mod.developer),
                licenseId = mod.licenseId,
                iconPath = mod.iconPath,
                environment = mod.configEnvironment,
                widenerConfigPath = mod.accessorConfigPath.takeIf { hasAccessor },
                mixinConfigPath = listOf(mod.mixinConfigPath),
                links = OrnitheModLinksConfig.of(mod),
                entryPoints = OrnitheModEntryPointsConfig.of(mod),
                dependencies = OrnitheModDependenciesConfig.of(mod),
            )
    }
}
