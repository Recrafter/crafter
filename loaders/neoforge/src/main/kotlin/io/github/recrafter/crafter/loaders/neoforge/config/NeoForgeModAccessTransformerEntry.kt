package io.github.recrafter.crafter.loaders.neoforge.config

import io.github.recrafter.crafter.core.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeoForgeModAccessTransformerEntry(
    @SerialName("file")
    val widenerConfigPath: String,
) {
    companion object {
        fun of(mod: Mod): NeoForgeModAccessTransformerEntry =
            NeoForgeModAccessTransformerEntry(
                widenerConfigPath = mod.accessorConfigPath
            )
    }
}
