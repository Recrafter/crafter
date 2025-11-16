package io.github.recrafter.crafter.sync.common

import io.github.diskria.kotlin.utils.serialization.annotations.PrettyPrint
import kotlinx.serialization.Serializable

@Serializable
@PrettyPrint
class MinecraftComponents(
    val versions: List<MinecraftComponent>,
    val lastSyncMillis: Long,
)
