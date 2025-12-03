package io.github.recrafter.crafter.cli.bash.api.commands.arguments

import io.github.recrafter.crafter.cli.bash.references.VariableReference

sealed interface Argument {

    val name: String
    val description: String

    val variable: VariableReference
        get() = VariableReference.of(name)
}
