package io.github.recrafter.crafter.cli.shell.arguments

import io.github.recrafter.crafter.cli.shell.ShellScriptBuilder

class ArgumentReference(val name: String) {
    override fun toString(): String = ShellScriptBuilder.getShellVar(name)
}
