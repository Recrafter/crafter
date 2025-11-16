package io.github.recrafter.crafter.configs.packs.resources

import io.github.diskria.kotlin.utils.serialization.annotations.PrettyPrint
import io.github.recrafter.crafter.configs.packs.common.PackConfig
import io.github.recrafter.crafter.models.Mod
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
