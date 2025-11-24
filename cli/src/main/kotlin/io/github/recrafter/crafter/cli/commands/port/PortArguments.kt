package io.github.recrafter.crafter.cli.commands.port

import io.github.recrafter.crafter.cli.bash.arguments.CLIArguments
import io.github.recrafter.crafter.cli.commands.api.annotations.CLICommandEnumArgument
import io.github.recrafter.crafter.cli.commands.api.annotations.CLICommandStringArgument
import kotlinx.serialization.Serializable

@Serializable
data class PortArguments(
    @CLICommandStringArgument(description = "Name of mod loader")
    val loader: String,

    @CLICommandEnumArgument("Porting workflow step", PortStepType::class)
    val step: String,

    @CLICommandEnumArgument("Version direction", PortDirectionType::class)
    val direction: String,

    @CLICommandEnumArgument("Test strategy", PortStrategyType::class)
    val strategy: String,
) : CLIArguments()
