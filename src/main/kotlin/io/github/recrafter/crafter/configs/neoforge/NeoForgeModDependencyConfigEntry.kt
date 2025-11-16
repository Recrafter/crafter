package io.github.recrafter.crafter.configs.neoforge

import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.models.Mod
import io.github.recrafter.crafter.versions.VersionBound
import io.github.recrafter.crafter.versions.range.IntervalVersionRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeoForgeModDependencyConfigEntry(
    @SerialName("modId")
    val id: String,

    val type: String,

    @SerialName("versionRange")
    val version: String,

    val ordering: String,
    val side: String,
) {
    companion object {
        fun of(id: String, version: String): NeoForgeModDependencyConfigEntry =
            NeoForgeModDependencyConfigEntry(
                id = id,
                type = "required",
                version = version,
                ordering = "NONE",
                side = "BOTH",
            )

        fun createMinecraftDependency(mod: Mod): NeoForgeModDependencyConfigEntry =
            of(
                id = "minecraft",
                version = IntervalVersionRange.min(VersionBound.inclusive(mod.minecraftVersion.asString())),
            )

        fun createLoaderDependency(mod: Mod): NeoForgeModDependencyConfigEntry =
            of(
                id = "neoforge",
                version = IntervalVersionRange.min(VersionBound.inclusive(mod.versions.loader)),
            )
    }
}
