package io.github.recrafter.crafter.helpers.shell.syntax

import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.extensions.common.modifyIf
import io.github.diskria.kotlin.utils.extensions.wrapWithBrackets
import io.github.diskria.kotlin.utils.extensions.wrapWithSpace
import io.github.recrafter.crafter.extensions.common.shellScript
import io.github.recrafter.crafter.helpers.shell.ShellScriptBuilder

class ShellIf internal constructor(
    val conditions: List<String>,
    val booleanOperator: ShellBooleanOperator,
    val body: String,
) {
    val isElse: Boolean = conditions.isEmpty()

    fun generateScript(previousIf: ShellIf?, nextIf: ShellIf?): String = shellScript {
        if (isElse) {
            code { "else" }
        } else {
            code {
                val token = ShellKeyword.IF.token.modifyIf(previousIf != null) { "el$it" }
                val bracketedConditions = conditions.map { it.wrapWithSpace().wrapWithBrackets(BracketsType.SQUARE) }
                "$token ${bracketedConditions.joinToString(booleanOperator.token)}; then"
            }
        }
        withIndent {
            code { body }
        }
        if (nextIf == null) {
            code {
                ShellKeyword.IF.end
            }
        }
        return@shellScript this
    }

    companion object {
        fun ofIf(
            conditions: List<String>,
            booleanOperator: ShellBooleanOperator,
            builder: ShellScriptBuilder.() -> ShellScriptBuilder
        ): ShellIf =
            ShellIf(conditions, booleanOperator, shellScript(builder))

        fun ofIf(condition: String, builder: ShellScriptBuilder.() -> ShellScriptBuilder): ShellIf =
            ShellIf(listOf(condition), ShellBooleanOperator.AND, shellScript(builder))

        fun ofElse(builder: ShellScriptBuilder.() -> ShellScriptBuilder): ShellIf =
            ofIf(emptyList(), ShellBooleanOperator.AND, builder)
    }
}
