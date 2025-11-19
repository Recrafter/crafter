package io.github.recrafter.crafter.helpers.shell.arguments

import io.github.recrafter.crafter.helpers.shell.ShellHelper

class ArgumentReference(val name: String) {
    override fun toString(): String = ShellHelper.variable(name)
}
