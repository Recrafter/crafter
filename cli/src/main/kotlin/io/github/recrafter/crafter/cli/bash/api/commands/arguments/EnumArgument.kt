package io.github.recrafter.crafter.cli.bash.api.commands.arguments

import io.github.recrafter.crafter.cli.bash.api.commands.arguments.enums.EnumArgumentOption

data class EnumArgument(
    override val name: String,
    override val description: String,
    val options: List<EnumArgumentOption>,
    val defaultOption: EnumArgumentOption?,
) : Argument
