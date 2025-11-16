package io.github.recrafter.crafter.configs.forge

import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.models.Mod
import io.github.recrafter.crafter.versions.VersionBound
import io.github.recrafter.crafter.versions.range.IntervalVersionRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForgeModDependencyConfigEntry(
    @SerialName("modId")
    val id: String,

    @SerialName("mandatory")
    val isRequired: Boolean,

    @SerialName("versionRange")
    val version: String,

    val ordering: String,
    val side: String,
) {
    companion object {
        fun of(id: String, version: String): ForgeModDependencyConfigEntry =
            ForgeModDependencyConfigEntry(
                id = id,
                isRequired = true,
                version = version,
                ordering = "NONE",
                side = "BOTH",
            )

        fun createMinecraftDependency(mod: Mod): ForgeModDependencyConfigEntry =
            of(
                id = "minecraft",
                version = IntervalVersionRange.min(VersionBound.inclusive(mod.minecraftVersion.asString())),
            )

        fun createLoaderDependency(mod: Mod): ForgeModDependencyConfigEntry =
            of(
                id = "forge",
                version = IntervalVersionRange.min(VersionBound.inclusive(mod.versions.loader)),
            )
    }
}
