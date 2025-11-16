package io.github.recrafter.crafter.configs.neoforge

import io.github.recrafter.crafter.models.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeoForgeModAccessTransformerConfigEntry(
    @SerialName("file")
    val accessConfigPath: String,
) {
    companion object {
        fun of(mod: Mod): NeoForgeModAccessTransformerConfigEntry =
            NeoForgeModAccessTransformerConfigEntry(
                accessConfigPath = mod.accessConfigPath
            )
    }
}
