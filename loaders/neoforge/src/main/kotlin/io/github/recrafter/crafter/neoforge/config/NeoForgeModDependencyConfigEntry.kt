package io.github.recrafter.crafter.neoforge.config

import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.MinecraftConstants
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.versions.VersionBound
import io.github.recrafter.crafter.core.versions.range.IntervalVersionRange
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
                id = MinecraftConstants.FULL_GAME_NAME.lowercase(),
                version = IntervalVersionRange.min(VersionBound.inclusive(mod.minecraftVersion.asString())),
            )

        fun createLoaderDependency(mod: Mod): NeoForgeModDependencyConfigEntry =
            of(
                id = ModLoaderType.NEOFORGE.getName(),
                version = IntervalVersionRange.min(VersionBound.inclusive(mod.versions.loader)),
            )
    }
}
