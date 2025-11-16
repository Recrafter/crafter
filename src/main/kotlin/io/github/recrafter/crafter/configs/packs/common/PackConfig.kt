package io.github.recrafter.crafter.configs.packs.common

import io.github.recrafter.bedrock.era.Release
import io.github.recrafter.bedrock.versions.compareTo
import io.github.recrafter.crafter.models.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackConfig(
    @SerialName("pack_format")
    val format: Int? = null,

    val description: String,

    @SerialName("min_format")
    val minFormat: String? = null,
) {
    companion object {
        fun of(mod: Mod, format: String): PackConfig {
            val description = "${mod.name} resources"
            return when {
                mod.minecraftVersion < Release.V_1_21_9 -> PackConfig(
                    format = format.toInt(),
                    description = description,
                )

                else -> PackConfig(
                    description = description,
                    minFormat = format,
                )
            }
        }
    }
}
