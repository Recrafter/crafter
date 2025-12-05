package io.github.recrafter.crafter.cli.commands.port

import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommandEnumArgument
import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommandStringArgument
import io.github.recrafter.crafter.cli.bash.api.commands.arguments.common.CLIArguments
import io.github.recrafter.crafter.cli.bash.variables.EnumVar
import io.github.recrafter.crafter.cli.bash.variables.StringVar
import kotlinx.serialization.Serializable

@Serializable
data class PortArguments(
    @CLICommandStringArgument(description = "Name of mod loader")
    val loader: StringVar,

    @CLICommandEnumArgument("Porting workflow step", PortStep::class)
    val step: EnumVar<PortStep>,

    @CLICommandEnumArgument("Porting direction", PortDirection::class)
    val direction: EnumVar<PortDirection>,

    @CLICommandEnumArgument("Porting test strategy", PortStrategy::class)
    val strategy: EnumVar<PortStrategy>,
) : CLIArguments()
