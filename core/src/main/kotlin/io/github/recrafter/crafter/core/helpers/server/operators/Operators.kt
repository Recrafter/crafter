package io.github.recrafter.crafter.core.helpers.server.operators

import io.github.diskria.kotlin.utils.serialization.annotations.PrettyPrint
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
@PrettyPrint
value class Operators(val entries: List<OperatorEntry>)
