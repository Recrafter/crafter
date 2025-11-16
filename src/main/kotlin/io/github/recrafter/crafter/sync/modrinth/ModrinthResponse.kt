package io.github.recrafter.crafter.sync.modrinth

import io.github.diskria.kotlin.utils.serialization.annotations.IgnoreUnknownKeys
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
@IgnoreUnknownKeys
value class ModrinthResponse(val versions: List<ModrinthVersion>)
