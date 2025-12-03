package io.github.recrafter.crafter.cli.commands.craft

import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommandEnumArgument
import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommandStringArgument
import io.github.recrafter.crafter.cli.bash.api.commands.arguments.common.CLIArguments
import io.github.recrafter.crafter.cli.bash.variables.StringVar
import kotlinx.serialization.Serializable

@Serializable
data class CraftArguments(
    @CLICommandStringArgument(description = "Name of mod loader")
    val loader: StringVar,

    @CLICommandStringArgument(description = "Target Minecraft version")
    val version: StringVar,

    @CLICommandEnumArgument(description = "Game side to build and launch", enumClass = CraftSideType::class)
    val side: StringVar,
) : CLIArguments()
