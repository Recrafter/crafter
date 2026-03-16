package io.github.recrafter.crafter.loaders.quilt.config

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
    val mixinConfigPath: String,

    @SerialName("access_widener")
    val widenerConfigPath: String?,
) {
    companion object {
        fun of(mod: Mod, hasAccessor: Boolean): QuiltModConfig =
            QuiltModConfig(
                schemaVersion = 1,
                loader = QuiltModLoaderConfig.of(mod),
                mixinConfigPath = mod.mixinConfigPath,
                widenerConfigPath = mod.accessorConfigPath.takeIf { hasAccessor },
            )
    }
}
