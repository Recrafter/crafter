package io.github.recrafter.crafter.cli.commands.common

import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.KotlinSerializer
import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.extensions.wrapWithBrackets
import io.github.diskria.kotlin.utils.extensions.wrapWithDoubleQuote
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.completion.ShellCompletion
import io.github.recrafter.crafter.cli.shell.ShellHelper
import io.github.recrafter.crafter.cli.shell.arguments.ArgumentReference
import io.github.recrafter.crafter.cli.shell.arguments.Arguments
import io.github.recrafter.crafter.cli.shell.syntax.ShellCase
import io.github.recrafter.crafter.cli.shell.syntax.ShellIf
import io.github.recrafter.crafter.cli.shell.syntax.ShellLogicalOperator
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

    val signature: String
        get() = name +
                Constants.Char.SPACE +
                arguments.joinBySpace { (name, _) -> name.wrapWithBrackets(BracketsType.ANGLE) }

    abstract fun run(fingerprint: Fingerprint, arguments: T): String

    abstract fun complete(
        fingerprint: Fingerprint,
        currentWordIndex: String,
        variants: (List<String>) -> String
    ): String

    fun generateRunCase(fingerprint: Fingerprint): ShellCase =
        ShellCase.of(name, aliases) {
            if (arguments.isNotEmpty()) {
                arguments.mapIndexed { index, (_, reference) ->
                    initVar(reference.name, getScriptArgument(index + 2))
                }
                shellIfThen(
                    ShellIf.ofIf(arguments.map { (_, reference) -> isEmpty(reference) }, ShellLogicalOperator.OR) {
                        printErr("Wrong $name command usage.")
                        shellPrintln("Usage: ${ShellHelper.terminalCommand(fingerprint.scriptName, signature)}")
                        throwException()
                    }
                )
            }
            code { run(fingerprint, createArguments()) }
        }

    fun generateCompletionCase(fingerprint: Fingerprint, currentWord: String): ShellCase =
        ShellCase.of(name, aliases) {
            arguments.forEach { (name, _) ->
                declareLocalVar(name)
            }
            code {
                complete(
                    fingerprint = fingerprint,
                    currentWordIndex = getVar(ShellCompletion.CURRENT_WORD),
                    variants = { variants ->
                        val variantsString = variants.joinBySpace().wrapWithDoubleQuote()
                        val completionReply = sh.invoke(ShellCompletion.GENERATE, "-W $variantsString -- $currentWord")
                        sh.initArray(ShellCompletion.REPLY, completionReply)
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
