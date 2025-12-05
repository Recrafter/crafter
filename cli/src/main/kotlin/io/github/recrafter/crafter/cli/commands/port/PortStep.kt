package io.github.recrafter.crafter.cli.commands.port

import io.github.recrafter.crafter.cli.bash.api.commands.arguments.enums.CLIArgumentEnum

enum class PortStep(override val description: String) : CLIArgumentEnum<PortStep> {
    START("Begin porting to the next version"),
    CONTINUE("Current version works — proceed to the next one"),
    STOP("Current version broken — stop here for fixes"),
    CANCEL("Abort current porting and remove temporary port folder"),
}
