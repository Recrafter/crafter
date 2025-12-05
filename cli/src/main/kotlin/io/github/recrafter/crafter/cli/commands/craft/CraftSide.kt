package io.github.recrafter.crafter.cli.commands.craft

import io.github.recrafter.crafter.cli.bash.api.commands.arguments.enums.CLIArgumentEnum

enum class CraftSide(override val description: String) : CLIArgumentEnum<CraftSide> {

    CLIENT("Build the mod and launch the Minecraft client"),
    SERVER("Build the mod and launch the Minecraft server"),
    LAUNCHER("Interactive mode for launching server and multiple clients");

    override val defaultEnum: CraftSide get() = CLIENT
}
