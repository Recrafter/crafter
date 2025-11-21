package io.github.recrafter.crafter.fabric.config

import io.github.diskria.kotlin.utils.serialization.annotations.EncodeDefaults
import io.github.diskria.kotlin.utils.serialization.annotations.PrettyPrint
import io.github.recrafter.crafter.core.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@EncodeDefaults
@PrettyPrint
data class FabricModConfig(
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
    val mixinsConfigPath: List<String>,

    @SerialName("accessWidener")
    val accessConfigPath: String,

    @SerialName("contact")
    val links: FabricModLinksConfig,

    @SerialName("entrypoints")
    val entryPoints: FabricModEntryPointsConfig,

    @SerialName("depends")
    val dependencies: FabricModDependenciesConfig,
) {
    companion object {
        fun of(mod: Mod): FabricModConfig =
            FabricModConfig(
                schemaVersion = 1,
                id = mod.id,
                version = mod.version,
                name = mod.name,
                description = mod.description,
                developers = listOf(mod.developer),
                licenseId = mod.licenseId,
                iconPath = mod.iconPath,
                environment = mod.configEnvironment,
                accessConfigPath = mod.accessConfigPath,
                mixinsConfigPath = listOf(mod.mixinsConfigPath),
                links = FabricModLinksConfig.of(mod),
                entryPoints = FabricModEntryPointsConfig.of(mod),
                dependencies = FabricModDependenciesConfig.of(mod),
            )
    }
}
