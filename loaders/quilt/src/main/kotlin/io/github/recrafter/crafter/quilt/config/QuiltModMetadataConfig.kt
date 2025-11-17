package io.github.recrafter.crafter.quilt.config

import io.github.recrafter.crafter.core.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuiltModMetadataConfig(
    val name: String,
    val description: String,

    @SerialName("contributors")
    val developers: Map<String, String>,

    @SerialName("contact")
    val links: QuiltModLinksConfig,

    @SerialName("icon")
    val iconPath: String,

    @SerialName("license")
    val licenseId: String,
) {
    companion object {
        fun of(mod: Mod): QuiltModMetadataConfig =
            QuiltModMetadataConfig(
                name = mod.name,
                description = mod.description,
                developers = mapOf(mod.developer to "Owner"),
                links = QuiltModLinksConfig.of(mod),
                iconPath = mod.iconPath,
                licenseId = mod.licenseId,
            )
    }
}
