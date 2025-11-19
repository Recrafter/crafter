package io.github.recrafter.crafter.tasks.cli.commands.common

import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.KotlinSerializer
import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.generics.joinByNewLine
import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.extensions.wrapWithBrackets
import io.github.diskria.kotlin.utils.extensions.wrapWithDoubleQuote
import io.github.diskria.kotlin.utils.extensions.wrapWithSpace
import io.github.recrafter.crafter.helpers.shell.ShellHelper
import io.github.recrafter.crafter.helpers.shell.arguments.ArgumentReference
import io.github.recrafter.crafter.helpers.shell.arguments.Arguments
import io.github.recrafter.crafter.helpers.shell.syntax.ShellCase
import io.github.recrafter.crafter.tasks.cli.Fingerprint
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

abstract class Command<T : Arguments>(
    val name: String,
    val description: String,
    val aliases: List<String>,
    private val serializer: KotlinSerializer<T>,
) {
    @OptIn(ExperimentalSerializationApi::class)
    val arguments: List<Pair<String, ArgumentReference>> by lazy {
        serializer.descriptor.elementNames.toList().map { name ->
            name to ArgumentReference(name.setCase(camelCase, SCREAMING_SNAKE_CASE))
        }
    }

    val schema: String
        get() = name +
                Constants.Char.SPACE +
                arguments.joinBySpace { (name, _) -> name.wrapWithBrackets(BracketsType.ANGLE) }

    abstract fun run(fingerprint: Fingerprint, arguments: T): String

    abstract fun complete(fingerprint: Fingerprint, currentWord: String, variants: (List<String>) -> String): String

    fun generateRunCase(fingerprint: Fingerprint): ShellCase =
        ShellHelper.case(name, aliases) {
            if (arguments.isNotEmpty()) {
                val conditions = arguments.joinToString(ShellHelper.OR.wrapWithSpace()) { (_, reference) ->
                    val condition = " -z $reference "
                    condition.wrapWithBrackets(BracketsType.SQUARE)
                }
                append {
                    arguments
                        .mapIndexed { index, (_, reference) ->
                            val scriptArgument = ShellHelper.variable(index + 2)
                            "${reference.name}=$scriptArgument"
                        }
                        .joinByNewLine()
                }
                append {
                    """
                    if $conditions; then
                      ${ShellHelper.echoRed("Wrong $name command usage.")}
                      ${ShellHelper.echo("Usage: ./${fingerprint.scriptFileName} $schema")}
                      ${ShellHelper.fail()}
                    fi
                    """
                }
            }
            append { run(fingerprint, createArguments()) }
        }

    @Suppress("SpellCheckingInspection")
    fun generateCompletionCase(fingerprint: Fingerprint): ShellCase =
        ShellHelper.case(name, aliases) {
            if (arguments.isNotEmpty()) {
                append { "local " + arguments.joinBySpace { (name, _) -> name } }
            }
            append {
                complete(
                    fingerprint = fingerprint,
                    currentWord = ShellHelper.variable("COMP_CWORD"),
                    variants = { variants ->
                        val variantsString = variants.joinBySpace().wrapWithDoubleQuote()
                        val typingWord = ShellHelper.variable("typingWord")
                        "COMPREPLY=($(compgen -W $variantsString -- $typingWord))"
                    },
                )
            }
        }

    private fun createArguments(): T =
        Json.decodeFromJsonElement(
            serializer,
            buildJsonObject {
                arguments.forEach { (name, reference) -> put(name, JsonPrimitive(reference.toString())) }
            }
        )
}
