package io.github.recrafter.crafter.cli.commands.craft

import io.github.recrafter.crafter.cli.commands.api.common.CLIArgumentEnum

enum class CraftSideType(override val description: String) : CLIArgumentEnum<CraftSideType> {

    CLIENT("Build the mod and launch the Minecraft client"),
    SERVER("Build the mod and launch the Minecraft server"),
    BOTH("Build the mod, launch the server, then the client");

    override val defaultEnum: CraftSideType get() = CLIENT
}
