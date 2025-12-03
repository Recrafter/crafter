package io.github.recrafter.crafter.cli.commands.craft

import io.github.recrafter.crafter.cli.bash.api.commands.arguments.enums.CLIArgumentEnum

enum class CraftSideType(override val description: String) : CLIArgumentEnum<CraftSideType> {

    CLIENT("Build the mod and launch the Minecraft client"),
    SERVER("Build the mod and launch the Minecraft server"),
    MERGED("Build the mod, launch the server, then the client");

    override val defaultEnum: CraftSideType get() = CLIENT
}
