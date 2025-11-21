package io.github.recrafter.crafter.cli.commands.init

import io.github.recrafter.crafter.cli.shell.arguments.Arguments
import kotlinx.serialization.Serializable

@Serializable
data class InitArguments(
    val loader: String,
    val version: String,
) : Arguments()
