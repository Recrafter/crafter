package io.github.recrafter.crafter.core.sync.common

import io.github.recrafter.bedrock.versions.MinecraftVersion
import kotlinx.serialization.Serializable

@Serializable
data class MinecraftComponent(
    val minecraftVersion: MinecraftVersion,
    val latestVersion: String,
) {
    var isLatestRelease: Boolean = false
}
