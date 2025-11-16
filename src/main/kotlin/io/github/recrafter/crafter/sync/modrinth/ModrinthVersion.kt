package io.github.recrafter.crafter.sync.modrinth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModrinthVersion(
    @SerialName("game_versions")
    val supportedMinecraftVersions: List<String>,

    @SerialName("version_number")
    val versionNumber: String,
)
