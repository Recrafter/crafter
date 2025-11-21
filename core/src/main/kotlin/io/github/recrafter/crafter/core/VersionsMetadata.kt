package io.github.recrafter.crafter.core

import kotlinx.serialization.Serializable

@Serializable
data class VersionsMetadata(
    val loader: String,
    val mappings: String? = null,
    val mappingsMinecraft: String? = null,
)
