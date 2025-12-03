package io.github.recrafter.crafter.cli.commands.port

import io.github.recrafter.crafter.cli.bash.api.commands.arguments.enums.CLIArgumentEnum

enum class PortDirectionType(override val description: String) : CLIArgumentEnum<PortDirectionType> {

    PAST("Port to older Minecraft versions (backport)"),
    FUTURE("Port to newer Minecraft versions (forward-port)");

    override val defaultEnum: PortDirectionType get() = PAST
}
