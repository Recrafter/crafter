package io.github.recrafter.crafter.cli.commands.common

import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.shell.arguments.NoArguments

abstract class SimpleCommand(
    name: String,
    description: String,
    aliases: List<String>,
) : Command<NoArguments>(name, description, aliases, NoArguments.serializer()) {

    abstract fun run(fingerprint: Fingerprint): String

    final override fun run(fingerprint: Fingerprint, arguments: NoArguments): String =
        run(fingerprint)
}
