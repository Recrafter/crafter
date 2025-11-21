package io.github.recrafter.crafter.helpers.shell.arguments

import io.github.recrafter.crafter.helpers.shell.ShellScriptBuilder

class ArgumentReference(val name: String) {
    override fun toString(): String = ShellScriptBuilder.getShellVar(name)
}
