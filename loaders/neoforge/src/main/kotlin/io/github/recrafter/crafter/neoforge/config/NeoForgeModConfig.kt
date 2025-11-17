package io.github.recrafter.crafter.neoforge.config

import io.github.recrafter.crafter.core.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeoForgeModConfig(
    @SerialName("license")
    val licenseId: String,

    @SerialName("issueTrackerURL")
    val issuesUrl: String?,

    val mods: List<NeoForgeModConfigEntry>,
    val mixins: List<NeoForgeModMixinConfigEntry>,
    val accessTransformers: List<NeoForgeModAccessTransformerConfigEntry>,
    val dependencies: Map<String, List<NeoForgeModDependencyConfigEntry>>,
) {
    companion object {
        fun of(mod: Mod): NeoForgeModConfig =
            NeoForgeModConfig(
                licenseId = mod.licenseId,
                issuesUrl = mod.issuesUrl,
                mods = listOf(
                    NeoForgeModConfigEntry.of(mod),
                ),
                mixins = listOf(
                    NeoForgeModMixinConfigEntry.of(mod),
                ),
                accessTransformers = listOf(
                    NeoForgeModAccessTransformerConfigEntry.of(mod),
                ),
                dependencies = mapOf(
                    mod.id to listOf(
                        NeoForgeModDependencyConfigEntry.createMinecraftDependency(mod),
                        NeoForgeModDependencyConfigEntry.createLoaderDependency(mod),
                    )
                ),
            )
    }
}
