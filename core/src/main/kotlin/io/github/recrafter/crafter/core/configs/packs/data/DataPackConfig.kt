package io.github.recrafter.crafter.core.configs.packs.data

import io.github.diskria.kotlin.utils.serialization.annotations.PrettyPrint
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.configs.packs.common.PackConfig
import kotlinx.serialization.Serializable

@Serializable
@PrettyPrint
data class DataPackConfig(
    val pack: PackConfig,
) {
    companion object {
        fun of(mod: Mod, minFormat: String, maxFormat: String = minFormat): DataPackConfig =
            DataPackConfig(
                pack = PackConfig.of(mod, minFormat, maxFormat)
            )
    }
}
