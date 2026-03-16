package io.github.recrafter.crafter.core.configs.packs.common

import io.github.diskria.kotlin.utils.Constants
import io.github.recrafter.bedrock.versions.compareTo
import io.github.recrafter.crafter.core.LoaderCompatibility
import io.github.recrafter.crafter.core.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackConfig(
    val description: String,

    @SerialName("pack_format")
    val format: Int? = null,

    @SerialName("min_format")
    val minFormat: List<Int>? = null,

    @SerialName("max_format")
    val maxFormat: List<Int>? = null,
) {
    companion object {
        fun of(mod: Mod, minFormat: String, maxFormat: String = minFormat): PackConfig {
            val description = "${mod.name} resources"
            if (mod.minecraftVersion < LoaderCompatibility.Forge.PACK_CONFIG_RANGES) {
                return PackConfig(
                    format = minFormat.toInt(),
                    description = description,
                )
            }
            return PackConfig(
                description = description,
                minFormat = splitToMajorMinor(minFormat),
                maxFormat = splitToMajorMinor(maxFormat),
            )
        }

        private fun splitToMajorMinor(format: String): List<Int> =
            if (format.contains(Constants.Char.DOT)) format.split(Constants.Char.DOT).map { it.toInt() }
            else listOf(format.toInt(), 0)
    }
}
