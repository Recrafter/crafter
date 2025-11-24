package io.github.recrafter.crafter.cli.commands.craft

import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.bash.builder.getValue
import io.github.recrafter.crafter.cli.bash.builder.quotedValue
import io.github.recrafter.crafter.cli.commands.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.commands.api.common.AbstractCLICommand
import io.github.recrafter.crafter.cli.extensions.common.bashScript
import io.github.recrafter.crafter.cli.extensions.common.withBashScript

@CLICommand(name = "craft", description = "Build the mod and launch the selected Minecraft side for development")
object CraftCommand : AbstractCLICommand<CraftArguments>(CraftArguments.serializer()) {

    override fun getCompletions(argumentName: String, arguments: CraftArguments): String = withBashScript {
        when (argumentName) {
            arguments::loader.name -> bash.getStringVar("LOADERS").quotedValue
            arguments::version.name -> bash.getMapVar("VERSIONS").getValue(arguments.loader)
            else -> failWithInvalidValue(argumentName)
        }
    }

    override fun run(fingerprint: Fingerprint, arguments: CraftArguments): String = bashScript {
        code {
            ""
        }
    }
}
