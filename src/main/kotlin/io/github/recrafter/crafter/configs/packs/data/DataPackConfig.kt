package io.github.recrafter.crafter.configs.packs.data

import io.github.diskria.kotlin.utils.serialization.annotations.PrettyPrint
import io.github.recrafter.crafter.configs.packs.common.PackConfig
import io.github.recrafter.crafter.models.Mod
import kotlinx.serialization.Serializable

@Serializable
@PrettyPrint
data class DataPackConfig(
    val pack: PackConfig,
) {
    companion object {
        fun of(mod: Mod, format: String): DataPackConfig =
            DataPackConfig(
                pack = PackConfig.of(mod, format)
            )
    }
}
