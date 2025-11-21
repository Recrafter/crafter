package io.github.recrafter.crafter.cli.commands.init

import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.commands.common.Command
import io.github.recrafter.crafter.cli.completion.ShellCompletion
import io.github.recrafter.crafter.cli.extensions.common.shellScript
import io.github.recrafter.crafter.cli.shell.syntax.ShellCase
import io.github.recrafter.crafter.cli.shell.syntax.ShellIf

class InitCommand : Command<InitArguments>(
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

    override fun complete(
        fingerprint: Fingerprint,
        currentWordIndex: String,
        variants: (List<String>) -> String
    ): String = shellScript {
        initLocalVar("loader", getArrayValue(ShellCompletion.WORDS, 2))
        shellIfThen(
            ShellIf.ofIf("$currentWordIndex -eq 2") {
                code { variants(getLoaderNames(fingerprint)) }
            },
            ShellIf.ofIf("$currentWordIndex -eq 3") {
                withIndent {
                    shellWhen(
                        "loader",
                        fingerprint.loaderVersions.map { (loader, versions) ->
                            ShellCase.of(getLoaderName(loader)) {
                                code { variants(versions.map { it.asString() }) }
                            }
                        }
                    )
                }
            }
        )
    }

    private fun getLoaderNames(fingerprint: Fingerprint): List<String> =
        fingerprint.loaderVersions.keys.map { getLoaderName(it) }

    private fun getLoaderName(loader: ModLoaderType): String =
        loader.getName(`kebab-case`)
}
