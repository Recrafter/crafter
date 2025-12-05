package io.github.recrafter.crafter.cli.commands.port

import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.bash.api.commands.AbstractCLICommand
import io.github.recrafter.crafter.cli.bash.variables.quotedValue
import io.github.recrafter.crafter.cli.extensions.common.script
import io.github.recrafter.crafter.cli.extensions.common.withScript

@CLICommand(name = "port", description = "Port mod to older or newer versions")
object PortCommand : AbstractCLICommand<PortArguments>(PortArguments.serializer()) {

    override fun getCompletions(argumentName: String, arguments: PortArguments): String = withScript {
        when (argumentName) {
            arguments::loader.name -> bash.getString("LOADERS").quotedValue
            else -> failWithInvalidValue(argumentName)
        }
    }

    override fun run(fingerprint: Fingerprint, arguments: PortArguments): String = script {

        return@script this
    }
}
