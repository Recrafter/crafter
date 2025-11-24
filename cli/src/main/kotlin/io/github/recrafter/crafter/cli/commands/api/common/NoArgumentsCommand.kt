package io.github.recrafter.crafter.cli.commands.api.common

import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.bash.arguments.NoArguments

abstract class NoArgumentsCommand : AbstractCLICommand<NoArguments>(NoArguments.serializer()) {

    abstract fun run(fingerprint: Fingerprint): String

    final override fun run(fingerprint: Fingerprint, arguments: NoArguments): String =
        run(fingerprint)
}
