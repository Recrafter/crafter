package io.github.recrafter.crafter.loaders.fabric.config

import io.github.recrafter.bedrock.loaders.ModLoaderType
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

    @SerialName("fabric-language-kotlin")
    val kotlinVersion: String?,
) {
    companion object {
        fun of(mod: Mod): FabricModDependenciesConfig =
            FabricModDependenciesConfig(
                javaVersion = InequalityVersionRange.min(VersionBound.inclusive(mod.jvmTarget.toInt())),
                minecraftVersion = InequalityVersionRange.min(
                    VersionBound.inclusive(mod.minecraftVersion.normalizedSemver)
                ),
                loaderVersion = InequalityVersionRange.min(VersionBound.inclusive(mod.loaderMetadata.loaderVersion)),
                kotlinVersion = if (mod.loader == ModLoaderType.FABRIC) {
                    InequalityVersionRange.min(VersionBound.inclusive("1.13.7+kotlin.2.2.21"))
                } else null,
            )
    }
}
