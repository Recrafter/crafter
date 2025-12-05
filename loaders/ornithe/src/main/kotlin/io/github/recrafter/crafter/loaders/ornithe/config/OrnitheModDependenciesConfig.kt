package io.github.recrafter.crafter.loaders.ornithe.config

import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.extensions.toInt
import io.github.recrafter.crafter.core.versions.VersionBound
import io.github.recrafter.crafter.core.versions.range.InequalityVersionRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrnitheModDependenciesConfig(
    @SerialName("java")
    val javaVersion: String,

    @SerialName("fabricloader")
    val loaderVersion: String,
) {
    companion object {
        fun of(mod: Mod): OrnitheModDependenciesConfig =
            OrnitheModDependenciesConfig(
                javaVersion = InequalityVersionRange.min(VersionBound.inclusive(mod.jvmTarget.toInt())),
                loaderVersion = InequalityVersionRange.min(VersionBound.inclusive(mod.loaderMetadata.loaderVersion)),
            )
    }
}
