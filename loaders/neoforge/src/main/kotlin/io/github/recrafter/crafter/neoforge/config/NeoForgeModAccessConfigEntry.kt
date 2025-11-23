package io.github.recrafter.crafter.neoforge.config

import io.github.recrafter.crafter.core.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeoForgeModAccessConfigEntry(
    @SerialName("file")
    val accessConfigPath: String,
) {
    companion object {
        fun of(mod: Mod): NeoForgeModAccessConfigEntry =
            NeoForgeModAccessConfigEntry(
                accessConfigPath = mod.accessConfigPath
            )
    }
}
