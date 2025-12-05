@file:Suppress("UnusedReceiverParameter")

package io.github.recrafter.crafter.cli.extensions.common

import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder

typealias Builder<T> = T.() -> T

fun <T> Any.withScript(builder: ScriptBuilder.() -> T): T =
    builder(ScriptBuilder())

fun Any.script(builder: ScriptBuilder.() -> ScriptBuilder): String =
    builder(ScriptBuilder()).toString()

fun spaced(vararg segments: Any?): String =
    segments.toList().filterNotNull().joinBySpace()
