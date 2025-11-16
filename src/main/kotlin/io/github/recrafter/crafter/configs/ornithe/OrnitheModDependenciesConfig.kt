package io.github.recrafter.crafter.configs.ornithe

import io.github.recrafter.crafter.extensions.mappers.toInt
import io.github.recrafter.crafter.models.Mod
import io.github.recrafter.crafter.versions.VersionBound
import io.github.recrafter.crafter.versions.range.InequalityVersionRange
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
                javaVersion = InequalityVersionRange.min(VersionBound.inclusive(mod.jvmTarget.toInt().toString())),
                loaderVersion = InequalityVersionRange.min(VersionBound.inclusive(mod.versions.loader)),
            )
    }
}
