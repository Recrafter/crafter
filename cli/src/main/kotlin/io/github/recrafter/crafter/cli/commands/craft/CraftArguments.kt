package io.github.recrafter.crafter.cli.commands.craft

import io.github.recrafter.crafter.cli.bash.arguments.CLIArguments
import io.github.recrafter.crafter.cli.commands.api.annotations.CLICommandEnumArgument
import io.github.recrafter.crafter.cli.commands.api.annotations.CLICommandStringArgument
import kotlinx.serialization.Serializable

@Serializable
data class CraftArguments(
    @CLICommandStringArgument(description = "Name of mod loader")
    val loader: String,

    @CLICommandStringArgument(description = "Target Minecraft version")
    val version: String,

    @CLICommandEnumArgument(description = "Game side to build and launch", enumClass = CraftSideType::class)
    val side: String,
) : CLIArguments()
