package io.github.recrafter.crafter.quilt.config

import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.versions.VersionBound
import io.github.recrafter.crafter.core.versions.range.InequalityVersionRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuiltModDependencyConfig(
    val id: String,

    @SerialName("versions")
    val version: String,
) {
    companion object {
        fun of(id: String, version: String): QuiltModDependencyConfig =
            QuiltModDependencyConfig(
                id = id,
                version = version,
            )

        fun createJavaDependency(mod: Mod): QuiltModDependencyConfig =
            of(
                id = "java",
                version = InequalityVersionRange.min(VersionBound.inclusive(mod.minJavaVersion.toString())),
            )

        fun createMinecraftDependency(mod: Mod): QuiltModDependencyConfig =
            of(
                id = "minecraft",
                version = InequalityVersionRange.min(VersionBound.inclusive(mod.minecraftVersion.asString())),
            )

        fun createLoaderDependency(mod: Mod): QuiltModDependencyConfig =
            of(
                id = "quilt_loader",
                version = InequalityVersionRange.min(VersionBound.inclusive(mod.versions.loader)),
            )
    }
}
