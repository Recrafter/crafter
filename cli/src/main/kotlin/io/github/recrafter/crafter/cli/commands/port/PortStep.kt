package io.github.recrafter.crafter.cli.commands.port

import io.github.recrafter.crafter.cli.bash.api.commands.arguments.enums.CLIArgumentEnum

enum class PortStep(override val description: String) : CLIArgumentEnum<PortStep> {
    START("Start porting to the next Minecraft version"),
    TEST("Re-test the mod on the current target version"),
    CONTINUE("The mod works on this version — proceed to the next one"),
    STOP("The mod is broken on this version — stop porting here"),
    CANCEL("Cancel the current port and delete the temporary project"),
}
