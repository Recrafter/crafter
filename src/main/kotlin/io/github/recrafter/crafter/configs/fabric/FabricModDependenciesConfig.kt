package io.github.recrafter.crafter.configs.fabric

import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.extensions.mappers.toInt
import io.github.recrafter.crafter.models.Mod
import io.github.recrafter.crafter.versions.VersionBound
import io.github.recrafter.crafter.versions.range.InequalityVersionRange
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
                javaVersion = InequalityVersionRange.min(VersionBound.inclusive(mod.jvmTarget.toInt().toString())),
                minecraftVersion = InequalityVersionRange.min(VersionBound.inclusive(mod.minecraftVersion.asString())),
                loaderVersion = InequalityVersionRange.min(VersionBound.inclusive(mod.versions.loader)),
            )
    }
}
