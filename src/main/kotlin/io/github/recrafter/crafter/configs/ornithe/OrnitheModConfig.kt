package io.github.recrafter.crafter.configs.ornithe

import io.github.diskria.kotlin.utils.serialization.annotations.EncodeDefaults
import io.github.diskria.kotlin.utils.serialization.annotations.PrettyPrint
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.models.Mod
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
    val mixinsConfigPath: List<String>,

    @SerialName("accessWidener")
    val accessConfigPath: String,

    @SerialName("contact")
    val links: OrnitheModLinksConfig,

    @SerialName("entrypoints")
    val entryPoints: OrnitheModEntryPointsConfig,

    @SerialName("depends")
    val dependencies: OrnitheModDependenciesConfig,
) {
    companion object {
        fun of(mod: Mod, splitSide: ModSide?): OrnitheModConfig =
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
                accessConfigPath = mod.accessConfigPath,
                mixinsConfigPath = listOf(mod.mixinsConfigPath),
                links = OrnitheModLinksConfig.of(mod),
                entryPoints = OrnitheModEntryPointsConfig.of(mod, splitSide),
                dependencies = OrnitheModDependenciesConfig.of(mod),
            )
    }
}
