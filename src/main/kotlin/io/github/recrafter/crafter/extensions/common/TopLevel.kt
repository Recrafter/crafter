package io.github.recrafter.crafter.extensions.common

import io.github.recrafter.crafter.helpers.shell.ScriptBuilder

fun buildScript(builder: ScriptBuilder.() -> ScriptBuilder): String =
    builder(ScriptBuilder()).toString()
