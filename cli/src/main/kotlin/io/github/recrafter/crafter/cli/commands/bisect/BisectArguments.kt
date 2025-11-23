package io.github.recrafter.crafter.cli.commands.bisect

import io.github.recrafter.crafter.cli.shell.arguments.Arguments
import kotlinx.serialization.Serializable

@Serializable
data class BisectArguments(
    val loader: String,
    val command: String,
    val future: String,
) : Arguments()
