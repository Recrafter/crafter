package io.github.recrafter.crafter.loaders.forge.config

import io.github.diskria.kotlin.utils.Constants
import io.github.recrafter.bedrock.sides.ModEnvironment
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.versions.VersionBound
import io.github.recrafter.crafter.core.versions.range.IntervalVersionRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForgeModConfig(
    val modLoader: String,

    val loaderVersion: String,

    @SerialName("license")
    val licenseId: String,

    @SerialName("issueTrackerURL")
    val issuesUrl: String?,

    @SerialName("clientSideOnly")
    val isClientSideOnly: Boolean,

    val mods: List<ForgeModConfigEntry>,
    val dependencies: Map<String, List<ForgeModDependencyConfigEntry>>,
) {
    companion object {
        fun of(mod: Mod): ForgeModConfig =
            ForgeModConfig(
                modLoader = "javafml",
                loaderVersion = IntervalVersionRange.min(
                    VersionBound.inclusive(
                        mod.loaderMetadata.loaderVersion.substringBefore(Constants.Char.DOT)
                    )
                ),
                licenseId = mod.licenseId,
                issuesUrl = mod.issuesUrl,
                isClientSideOnly = mod.environment == ModEnvironment.CLIENT_ONLY,
                mods = listOf(
                    ForgeModConfigEntry.of(mod),
                ),
                dependencies = mapOf(
                    mod.id to listOf(
                        ForgeModDependencyConfigEntry.createMinecraftDependency(mod),
                        ForgeModDependencyConfigEntry.createLoaderDependency(mod),
                    )
                ),
            )
    }
}
