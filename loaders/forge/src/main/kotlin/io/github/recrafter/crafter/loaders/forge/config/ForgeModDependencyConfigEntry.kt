package io.github.recrafter.crafter.loaders.forge.config

import io.github.diskria.kotlin.utils.Constants
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
                id = MinecraftConstants.FULL_GAME_NAME.lowercase(),
                version = IntervalVersionRange.min(VersionBound.inclusive(mod.minecraftVersion.asString())),
            )

        fun createLoaderDependency(mod: Mod): ForgeModDependencyConfigEntry =
            of(
                id = ModLoaderType.FORGE.getName(),
                version = IntervalVersionRange.min(
                    VersionBound.inclusive(
                        mod.loaderMetadata.loaderVersion.substringBefore(Constants.Char.DOT)
                    )
                ),
            )
    }
}
