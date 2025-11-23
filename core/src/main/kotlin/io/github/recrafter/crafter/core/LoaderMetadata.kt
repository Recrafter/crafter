package io.github.recrafter.crafter.core

import io.github.recrafter.bedrock.versions.MinecraftVersion
import kotlinx.serialization.Serializable

@Serializable
data class LoaderMetadata(
    val loaderVersion: String,
    val mappingsVersion: String? = null,
    val minecraftVersion: MinecraftVersion,
)
