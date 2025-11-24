package io.github.recrafter.crafter.cli.commands.api.arguments

import io.github.recrafter.crafter.cli.bash.arguments.CLICommandArgumentReference

sealed interface Argument {
    val name: String
    val description: String

    val reference: CLICommandArgumentReference
        get() = CLICommandArgumentReference.of(name)
}
