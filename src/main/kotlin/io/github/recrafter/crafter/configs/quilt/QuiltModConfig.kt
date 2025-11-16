package io.github.recrafter.crafter.configs.quilt

import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.models.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuiltModConfig(
    @SerialName("schema_version")
    val schemaVersion: Int,

    @SerialName("quilt_loader")
    val loader: QuiltModLoaderConfig,

    @SerialName("mixin")
    val mixinsConfigPath: String,
) {
    companion object {
        fun of(mod: Mod, splitSide: ModSide?): QuiltModConfig =
            QuiltModConfig(
                schemaVersion = 1,
                loader = QuiltModLoaderConfig.of(mod, splitSide),
                mixinsConfigPath = mod.mixinsConfigPath,
            )
    }
}
