package io.github.recrafter.crafter.cli.bash.builder

import io.github.diskria.gradle.utils.extensions.common.requireGradle
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.generics.joinByNewLine
import io.github.recrafter.crafter.cli.bash.syntax.BashKeyword
import io.github.recrafter.crafter.cli.bash.syntax.BashOperator
import io.github.recrafter.crafter.cli.extensions.common.Builder
import io.github.recrafter.crafter.cli.extensions.common.bashScript
import io.github.recrafter.crafter.cli.extensions.singleQuoted

@Suppress("FunctionName")
class IfChainBuilder {

    fun build(): String =
        chain.withIndex().joinByNewLine { (index, branch) ->
            val isFirst = index == 0
            val isLast = index == chain.lastIndex
            bashScript {
                if (branch.isElse) {
                    code { BashKeyword.ELSE.token }
                } else {
                    code {
                        buildString {
                            append(
                                if (isFirst) BashKeyword.IF.token
                                else BashKeyword.ELIF.token
                            )
                            append(Constants.Char.SPACE)
                            append(branch.conditions.joinToString(branch.operator.token) {
                                it.expression
                            })
                            append(Constants.Char.SEMICOLON)
                            append(Constants.Char.SPACE)
                            append(BashKeyword.THEN.token)
                        }
                    }
                }
                withIndent {
                    code { branch.body }
                }
                if (isLast) {
                    code { BashKeyword.IF.closingToken }
                }
                return@bashScript this
            }
        }


    private val chain: MutableList<Branch> = mutableListOf()

    fun IfChainBuilder.if_(
        conditions: List<Condition>,
        operator: BashOperator = BashOperator.AND,
        isElse: Boolean = false,
        builder: Builder<BashScriptBuilder>
    ): IfChainBuilder {
        chain += Branch(conditions, operator, isElse, bashScript(builder = builder))
        return this
    }

    fun IfChainBuilder.if_(condition: Condition, builder: Builder<BashScriptBuilder>): IfChainBuilder =
        if_(listOf(condition), builder = builder)

    fun IfChainBuilder.else_(builder: Builder<BashScriptBuilder>): IfChainBuilder {
        requireGradle(chain.isNotEmpty()) {
            "else_() called before if_() — " +
                    "add at least one ${BashKeyword.IF.token.singleQuoted()} branch first."
        }
        requireGradle(chain.lastOrNull()?.isElse == false) {
            "else_() can only be called once — " +
                    " another ${BashKeyword.ELSE.token.singleQuoted()} branch already exists."
        }
        return if_(emptyList(), isElse = true, builder = builder)
    }

    class Branch(
        val conditions: List<Condition>,
        val operator: BashOperator,
        val isElse: Boolean,
        val body: String,
    )
}
