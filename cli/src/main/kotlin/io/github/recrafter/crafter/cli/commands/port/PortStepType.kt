package io.github.recrafter.crafter.cli.commands.port

import io.github.recrafter.crafter.cli.bash.api.commands.arguments.enums.CLIArgumentEnum

enum class PortStepType(override val description: String) : CLIArgumentEnum<PortStepType> {
    START("Begin porting to the next version"),
    CONTINUE("Current version works — proceed to the next one"),
    STOP("Current version broken — stop here for fixes")
}
