package io.github.recrafter.crafter.loaders.neoforge.config

import io.github.recrafter.crafter.core.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeoForgeModConfigEntry(
    @SerialName("modId")
    val id: String,

    val version: String,

    @SerialName("displayName")
    val name: String,

    @SerialName("updateJSONURL")
    val checkUpdatesUrl: String?,

    @SerialName("displayURL")
    val homepageUrl: String?,

    @SerialName("logoFile")
    val iconPath: String,

    @SerialName("logoBlur")
    val isIconLinearInterpolationEnabled: Boolean,

    @SerialName("authors")
    val developer: String,

    val description: String,
) {
    companion object {
        fun of(mod: Mod): NeoForgeModConfigEntry =
            NeoForgeModConfigEntry(
                id = mod.id,
                version = mod.version,
                name = mod.name,
                checkUpdatesUrl = null,
                homepageUrl = mod.homepageUrl,
                iconPath = mod.iconPath,
                isIconLinearInterpolationEnabled = false,
                developer = mod.developer,
                description = mod.description,
            )
    }
}
