package io.github.recrafter.crafter.tasks.cli.commands.init

import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.extensions.common.buildScript
import io.github.recrafter.crafter.helpers.shell.ShellHelper
import io.github.recrafter.crafter.tasks.cli.Fingerprint
import io.github.recrafter.crafter.tasks.cli.commands.common.Command

class InitCommand : Command<InitArguments>(
    name = "init",
    description = "Init a new mod project",
    aliases = listOf("i"),
    serializer = InitArguments.serializer(),
) {
    override fun run(fingerprint: Fingerprint, arguments: InitArguments): String = buildScript {
        append {
            ""
        }
    }

    override fun complete(
        fingerprint: Fingerprint,
        currentWord: String,
        variants: (List<String>) -> String
    ): String = buildScript {
        append {
            """
            loader=${ShellHelper.arrayElement("COMP_WORDS", index = 2)}
            if [ $currentWord -eq 2 ]; then
              ${variants(getLoaderNames(fingerprint))}
            elif [ $currentWord -eq 3 ]; then
            """
        }
        withIndent {
            append {
                ShellHelper.whenBy(
                    "loader",
                    fingerprint.loaderVersions.map { (loader, versions) ->
                        ShellHelper.case(getLoaderName(loader)) {
                            append { variants(versions.map { it.asString() }) }
                        }
                    }
                )
            }
        }
        append { "fi" }
    }

    private fun getLoaderNames(fingerprint: Fingerprint): List<String> =
        fingerprint.loaderVersions.keys.map { getLoaderName(it) }

    private fun getLoaderName(loader: ModLoaderType): String =
        loader.getName(`kebab-case`)
}
