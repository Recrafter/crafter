package io.github.recrafter.crafter.cli.commands.craft

import io.github.recrafter.crafter.cli.bash.api.commands.arguments.enums.CLIArgumentEnum

enum class CraftSide(override val description: String) : CLIArgumentEnum<CraftSide> {

    CLIENT("Build the mod and launch the Minecraft client"),
    SERVER("Build the mod and launch the Minecraft server"),
    MERGED("Build the mod, launch the server, then the client");

    override val defaultEnum: CraftSide get() = CLIENT
}
