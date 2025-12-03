package io.github.recrafter.crafter.cli.commands.init

import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommandStringArgument
import io.github.recrafter.crafter.cli.bash.api.commands.arguments.common.CLIArguments
import io.github.recrafter.crafter.cli.bash.variables.StringVar
import kotlinx.serialization.Serializable

@Serializable
data class InitArguments(
    @CLICommandStringArgument(description = "Name of mod loader")
    val loader: StringVar,

    @CLICommandStringArgument(description = "Target Minecraft version")
    val version: StringVar,
) : CLIArguments()
