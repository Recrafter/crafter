package io.github.recrafter.crafter.extensions.common

import io.github.recrafter.crafter.helpers.shell.ShellScriptBuilder

fun shellScript(builder: ShellScriptBuilder.() -> ShellScriptBuilder): String =
    builder(ShellScriptBuilder()).toString()
