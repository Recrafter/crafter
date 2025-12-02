package io.github.recrafter.crafter.neoforge.config

import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.versions.VersionBound
import io.github.recrafter.crafter.core.versions.range.IntervalVersionRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeoForgeModConfig(
    val modLoader: String,

    val loaderVersion: String,

    @SerialName("license")
    val licenseId: String,

    @SerialName("issueTrackerURL")
    val issuesUrl: String?,

    val mods: List<NeoForgeModConfigEntry>,
    val mixins: List<NeoForgeModMixinConfigEntry>,
    val accessTransformers: List<NeoForgeModAccessConfigEntry>,
    val dependencies: Map<String, List<NeoForgeModDependencyConfigEntry>>,
) {
    companion object {
        fun of(mod: Mod): NeoForgeModConfig =
            NeoForgeModConfig(
                modLoader = "javafml",
                loaderVersion = IntervalVersionRange.min(VersionBound.inclusive(1)),
                licenseId = mod.licenseId,
                issuesUrl = mod.issuesUrl,
                mods = listOf(
                    NeoForgeModConfigEntry.of(mod),
                ),
                mixins = listOf(
                    NeoForgeModMixinConfigEntry.of(mod),
                ),
                accessTransformers = listOf(
                    NeoForgeModAccessConfigEntry.of(mod),
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
