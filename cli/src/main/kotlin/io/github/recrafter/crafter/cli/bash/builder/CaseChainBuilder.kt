package io.github.recrafter.crafter.cli.bash.builder

import io.github.diskria.gradle.utils.extensions.common.requireGradle
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.generics.joinByNewLine
import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.recrafter.crafter.cli.bash.BashKeyword
import io.github.recrafter.crafter.cli.extensions.common.Builder
import io.github.recrafter.crafter.cli.extensions.common.script
import io.github.recrafter.crafter.cli.extensions.singleQuoted

@Suppress("FunctionName")
class CaseChainBuilder(val by: String) {

    private val chain: MutableList<Case> = mutableListOf()

    fun CaseChainBuilder.case_(
        match: String,
        isElse: Boolean = false,
        builder: Builder<ScriptBuilder>
    ): CaseChainBuilder {
        requireGradle(isElse || match != ELSE_MATCH) {
            "Do not pass $ELSE_MATCH to case_() — use else_() instead."
        }
        chain += Case(match, script(builder = builder))
        return this
    }

    fun CaseChainBuilder.case_(match: Int, builder: Builder<ScriptBuilder>): CaseChainBuilder =
        case_(match.toString(), builder = builder)

    fun CaseChainBuilder.else_(builder: Builder<ScriptBuilder>): CaseChainBuilder {
        requireGradle(chain.isNotEmpty()) {
            "else_() called before case() — " +
                    "add at least one ${BashKeyword.CASE.token.singleQuoted()} first."
        }
        requireGradle(chain.lastOrNull()?.isElse == false) {
            "else_() can only be called once — " +
                    "another ${BashKeyword.CASE.token.singleQuoted()} already exists."
        }
        return case_(ELSE_MATCH, isElse = true, builder = builder)
    }

    fun build(): String = script {
        code { listOf(BashKeyword.CASE.token, by, BashKeyword.IN.token).joinBySpace() }
        withIndent {
            code { chain.joinByNewLine { it.script } }
        }
        code { BashKeyword.ESAC.token }
    }

    data class Case(val match: String, val body: String) {

        val isElse: Boolean = match == ELSE_MATCH

        val script: String = script {
            code { match + Constants.Char.CLOSING_ROUND_BRACKET }
            withIndent {
                code { body }
            }
            code { Constants.Char.SEMICOLON.repeat(2) }
        }

        override fun toString(): String = script
    }

    companion object {
        private const val ELSE_MATCH: String = Constants.Char.ASTERISK.toString()
    }
}
