package io.github.recrafter.crafter.fabric.config

import io.github.recrafter.bedrock.era.common.MinecraftEra
import io.github.recrafter.bedrock.versions.asString
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
                javaVersion = InequalityVersionRange.min(VersionBound.inclusive(mod.jvmTarget.toInt().toString())),
                minecraftVersion = InequalityVersionRange.min(
                    VersionBound.inclusive(
                        when (mod.minecraftVersion.getEra()) {
                            MinecraftEra.RELEASE -> mod.minecraftVersion.asString()
                            MinecraftEra.BETA -> mod.minecraftVersion.getEnumVersion().replaceFirst("1.", "1.0.0-beta.")
                            else -> TODO()
                        }
                    )
                ),
                loaderVersion = InequalityVersionRange.min(VersionBound.inclusive(mod.versions.loader)),
            )
    }
}
