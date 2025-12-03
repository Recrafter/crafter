package io.github.recrafter.crafter.cli.commands.port

import io.github.recrafter.crafter.cli.bash.api.commands.arguments.enums.CLIArgumentEnum

enum class PortStrategyType(override val description: String) : CLIArgumentEnum<PortStrategyType> {

    PENDING("Build + launch game, wait for your approval"),
    AUTO("Build-only, no launch, continue on success, stop on crash");

    override val defaultEnum: PortStrategyType get() = PENDING
}
