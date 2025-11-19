package io.github.recrafter.crafter.tasks.cli.commands.init

import io.github.recrafter.crafter.helpers.shell.arguments.Arguments
import kotlinx.serialization.Serializable

@Serializable
data class InitArguments(
    val loader: String,
    val version: String,
) : Arguments()
