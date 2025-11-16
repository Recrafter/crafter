package io.github.recrafter.crafter.models

data class VersionsMetadata(
    val loader: String,
    val mappings: String? = null,
    val mappingsMinecraft: String? = null,
)
