package io.github.recrafter.crafter.tasks.cli.commands.common

import io.github.recrafter.crafter.helpers.shell.arguments.NoArguments
import io.github.recrafter.crafter.tasks.cli.Fingerprint

abstract class SimpleCommand(
    name: String,
    description: String,
    aliases: List<String>,
) : Command<NoArguments>(name, description, aliases, NoArguments.serializer()) {

    abstract fun run(fingerprint: Fingerprint): String

    final override fun run(fingerprint: Fingerprint, arguments: NoArguments): String =
        run(fingerprint)
}
