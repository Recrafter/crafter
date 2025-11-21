package io.github.recrafter.crafter.quilt.config

import io.github.diskria.kotlin.utils.serialization.annotations.PrettyPrint
import io.github.recrafter.crafter.core.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@PrettyPrint
data class QuiltModConfig(
    @SerialName("schema_version")
    val schemaVersion: Int,

    @SerialName("quilt_loader")
    val loader: QuiltModLoaderConfig,

    @SerialName("mixin")
    val mixinsConfigPath: String,
) {
    companion object {
        fun of(mod: Mod): QuiltModConfig =
            QuiltModConfig(
                schemaVersion = 1,
                loader = QuiltModLoaderConfig.of(mod),
                mixinsConfigPath = mod.mixinsConfigPath,
            )
    }
}
