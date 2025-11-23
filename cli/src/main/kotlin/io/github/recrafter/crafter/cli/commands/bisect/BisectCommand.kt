package io.github.recrafter.crafter.cli.commands.bisect

import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.commands.common.Command
import io.github.recrafter.crafter.cli.extensions.common.shellScript

object BisectCommand : Command<BisectArguments>(
    name = "drift",
    description = "Expand mod supported version range",
    aliases = listOf("d", "bisect"),
    serializer = BisectArguments.serializer(),
) {
    override fun run(fingerprint: Fingerprint, arguments: BisectArguments): String = shellScript {
        code {
            ""
        }
    }

    override fun complete(currentWordIndex: String, reply: (String) -> String): String = shellScript {
        code {
            ""
        }
    }
}
