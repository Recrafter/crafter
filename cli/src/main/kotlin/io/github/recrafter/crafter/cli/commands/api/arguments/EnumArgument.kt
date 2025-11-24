package io.github.recrafter.crafter.cli.commands.api.arguments

data class EnumArgument(
    override val name: String,
    override val description: String,
    val options: List<EnumArgumentOption>,
    val defaultOption: EnumArgumentOption?,
) : Argument
