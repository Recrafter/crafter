package io.github.recrafter.crafter.core.configs.packs.resources

import io.github.diskria.kotlin.utils.serialization.annotations.PrettyPrint
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.configs.packs.common.PackConfig
import kotlinx.serialization.Serializable

@Serializable
@PrettyPrint
data class ResourcePackConfig(
    val pack: PackConfig,
) {
    companion object {
        fun of(mod: Mod, format: String): ResourcePackConfig =
            ResourcePackConfig(
                pack = PackConfig.of(mod, format)
            )
    }
}
