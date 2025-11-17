package io.github.recrafter.crafter.core.sync.common

import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.MinecraftVersionSerializer
import kotlinx.serialization.Serializable

@Serializable
data class MinecraftComponent(
    @Serializable(with = MinecraftVersionSerializer::class)
    val minecraftVersion: MinecraftVersion,

    val latestVersion: String,
)
