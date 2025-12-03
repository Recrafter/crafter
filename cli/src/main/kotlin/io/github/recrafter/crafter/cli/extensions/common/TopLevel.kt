@file:Suppress("UnusedReceiverParameter")

package io.github.recrafter.crafter.cli.extensions.common

import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder

typealias Builder<T> = T.() -> T

fun <T> Any.withScript(builder: ScriptBuilder.() -> T): T =
    builder(ScriptBuilder())

fun Any.script(builder: ScriptBuilder.() -> ScriptBuilder): String =
    builder(ScriptBuilder()).toString()
