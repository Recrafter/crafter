package io.github.recrafter.crafter.tasks.cli.commands.init

import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.extensions.common.shellScript
import io.github.recrafter.crafter.helpers.shell.syntax.ShellCase
import io.github.recrafter.crafter.helpers.shell.syntax.ShellIf
import io.github.recrafter.crafter.tasks.cli.Fingerprint
import io.github.recrafter.crafter.tasks.cli.commands.common.Command

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
        initLocalVar("loader", getArrayValue("COMP_WORDS", 2))
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
