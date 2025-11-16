package io.github.recrafter.crafter.configs.neoforge

import io.github.recrafter.crafter.models.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeoForgeModMixinConfigEntry(
    @SerialName("config")
    val mixinsConfigPath: String,
) {
    companion object {
        fun of(mod: Mod): NeoForgeModMixinConfigEntry =
            NeoForgeModMixinConfigEntry(
                mixinsConfigPath = mod.mixinsConfigPath,
            )
    }
}
