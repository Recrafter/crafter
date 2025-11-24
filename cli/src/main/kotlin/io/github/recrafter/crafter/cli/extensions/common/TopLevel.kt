package io.github.recrafter.crafter.cli.extensions.common

import io.github.recrafter.crafter.cli.bash.builder.BashScriptBuilder

typealias Builder<T> = T.() -> T

fun <T> withBashScript(builder: BashScriptBuilder.() -> T): T =
    builder(BashScriptBuilder())

fun bashScript(builder: BashScriptBuilder.() -> BashScriptBuilder): String =
    builder(BashScriptBuilder()).toString()
