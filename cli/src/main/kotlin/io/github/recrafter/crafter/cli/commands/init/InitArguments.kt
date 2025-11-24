package io.github.recrafter.crafter.cli.commands.init

import io.github.recrafter.crafter.cli.bash.arguments.CLIArguments
import io.github.recrafter.crafter.cli.commands.api.annotations.CLICommandStringArgument
import kotlinx.serialization.Serializable

@Serializable
data class InitArguments(
    @CLICommandStringArgument(description = "Name of mod loader")
    val loader: String,

    @CLICommandStringArgument(description = "Target Minecraft version")
    val version: String,
) : CLIArguments()
