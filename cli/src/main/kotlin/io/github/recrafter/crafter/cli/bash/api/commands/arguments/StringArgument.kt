package io.github.recrafter.crafter.cli.bash.api.commands.arguments

data class StringArgument(
    override val name: String,
    override val description: String,
) : Argument
