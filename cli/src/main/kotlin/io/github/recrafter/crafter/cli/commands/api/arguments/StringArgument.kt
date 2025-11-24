package io.github.recrafter.crafter.cli.commands.api.arguments

data class StringArgument(
    override val name: String,
    override val description: String,
) : Argument
