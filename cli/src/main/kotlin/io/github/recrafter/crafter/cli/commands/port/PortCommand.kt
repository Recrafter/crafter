package io.github.recrafter.crafter.cli.commands.port

import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.bash.builder.quotedValue
import io.github.recrafter.crafter.cli.commands.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.commands.api.common.AbstractCLICommand
import io.github.recrafter.crafter.cli.extensions.common.bashScript
import io.github.recrafter.crafter.cli.extensions.common.withBashScript

@CLICommand(name = "port", description = "Port mod to older or newer versions")
object PortCommand : AbstractCLICommand<PortArguments>(PortArguments.serializer()) {

    override fun getCompletions(argumentName: String, arguments: PortArguments): String = withBashScript {
        when (argumentName) {
            arguments::loader.name -> bash.getStringVar("LOADERS").quotedValue
            else -> failWithInvalidValue(argumentName)
        }
    }

    override fun run(fingerprint: Fingerprint, arguments: PortArguments): String = bashScript {
        code {
            ""
        }
        println_(arguments.direction)
    }
}
