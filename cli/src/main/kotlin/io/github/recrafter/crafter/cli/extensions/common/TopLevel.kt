package io.github.recrafter.crafter.cli.extensions.common

import io.github.recrafter.crafter.cli.shell.ShellScriptBuilder

fun shellScript(builder: ShellScriptBuilder.() -> ShellScriptBuilder): String =
    builder(ShellScriptBuilder()).toString()
