package io.github.recrafter.crafter.sync.jfrog

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JFrogChild(
    val uri: String,

    @SerialName("folder")
    val isFolder: Boolean,
)
