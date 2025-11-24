package io.github.recrafter.crafter.fabric.config

import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.extensions.toInt
import io.github.recrafter.crafter.core.versions.VersionBound
import io.github.recrafter.crafter.core.versions.range.InequalityVersionRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FabricModDependenciesConfig(
    @SerialName("java")
    val javaVersion: String,

    @SerialName("minecraft")
    val minecraftVersion: String,

    @SerialName("fabricloader")
    val loaderVersion: String,
) {
    companion object {
        fun of(mod: Mod): FabricModDependenciesConfig =
            FabricModDependenciesConfig(
                javaVersion = InequalityVersionRange.min(VersionBound.inclusive(mod.jvmTarget.toInt())),
                minecraftVersion = InequalityVersionRange.min(
                    VersionBound.inclusive(mod.minecraftVersion.normalizedSemver)
                ),
                loaderVersion = InequalityVersionRange.min(VersionBound.inclusive(mod.loaderMetadata.loaderVersion)),
            )
    }
}
