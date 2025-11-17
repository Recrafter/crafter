package io.github.recrafter.crafter.forge.config

import io.github.recrafter.crafter.core.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForgeModConfigEntry(
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

    @SerialName("authors")
    val developer: String,

    val description: String,
) {
    companion object {
        fun of(mod: Mod): ForgeModConfigEntry =
            ForgeModConfigEntry(
                id = mod.id,
                version = mod.version,
                name = mod.name,
                checkUpdatesUrl = null,
                homepageUrl = mod.homepageUrl,
                iconPath = mod.iconPath,
                developer = mod.developer,
                description = mod.description,
            )
    }
}
