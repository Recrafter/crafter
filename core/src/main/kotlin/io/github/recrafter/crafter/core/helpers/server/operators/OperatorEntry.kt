package io.github.recrafter.crafter.core.helpers.server.operators

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OperatorEntry(
    val uuid: String,
    val name: String,
    val level: Int,

    @SerialName("bypassesPlayerLimit")
    val canBypassPlayerLimit: Boolean,
)
