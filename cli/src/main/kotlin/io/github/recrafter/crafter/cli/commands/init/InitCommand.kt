package io.github.recrafter.crafter.cli.commands.init

import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.commands.common.Command
import io.github.recrafter.crafter.cli.completion.ShellCompletion
import io.github.recrafter.crafter.cli.extensions.common.shellScript
import io.github.recrafter.crafter.cli.shell.syntax.ShellIf

object InitCommand : Command<InitArguments>(
    name = "init",
    description = "Init a new mod project",
    aliases = listOf("i"),
    serializer = InitArguments.serializer(),
) {
    override fun run(fingerprint: Fingerprint, arguments: InitArguments): String = shellScript {
        code {
            ""
        }
    }

    override fun complete(currentWordIndex: String, reply: (String) -> String): String = shellScript {
        val loader = initLocalVar("loader", sh.getArrayValue(ShellCompletion.WORDS, 2))
        buildIfThen(
            ShellIf.ofIf("$currentWordIndex -eq 2") {
                code { reply(sh.getVar("LOADERS")) }
            },
            ShellIf.ofIf("$currentWordIndex -eq 3") {
                withIndent {
                    code { reply(sh.getArrayValue("VERSIONS", loader)) }
                }
            }
        )
    }
}
