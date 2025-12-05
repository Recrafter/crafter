package io.github.recrafter.crafter.cli.bash.builder

import io.github.diskria.gradle.utils.extensions.common.requireGradle
import io.github.diskria.kotlin.utils.extensions.generics.joinByNewLine
import io.github.diskria.kotlin.utils.extensions.wrapWithSpace
import io.github.recrafter.crafter.cli.bash.BashKeyword
import io.github.recrafter.crafter.cli.bash.conditions.BashCondition
import io.github.recrafter.crafter.cli.bash.conditions.BashConditionStrategy
import io.github.recrafter.crafter.cli.extensions.common.Builder
import io.github.recrafter.crafter.cli.extensions.common.script
import io.github.recrafter.crafter.cli.extensions.common.spaced
import io.github.recrafter.crafter.cli.extensions.singleQuoted

@Suppress("FunctionName")
class IfChainBuilder {

    private val chain: MutableList<Branch> = mutableListOf()

    fun build(): String =
        chain.withIndex().joinByNewLine { (index, branch) ->
            val isFirst = index == 0
            val isLast = index == chain.lastIndex
            script {
                if (branch.isElse) {
                    code { BashKeyword.ELSE.token }
                } else {
                    code {
                        spaced(
                            if (isFirst) BashKeyword.IF else BashKeyword.ELIF,
                            branch.conditions
                                .joinToString(branch.strategy.operator.wrapWithSpace()) { it.expression }
                                .semicoloned(),
                            BashKeyword.THEN
                        )
                    }
                }
                withIndent {
                    code { branch.body }
                }
                if (isLast) {
                    code { BashKeyword.FI.token }
                }
                return@script this
            }
        }

    fun IfChainBuilder.ifAll(conditions: List<BashCondition>, builder: Builder<ScriptBuilder>): IfChainBuilder =
        if_(conditions, BashConditionStrategy.AND, builder = builder)

    fun IfChainBuilder.ifAll(vararg conditions: BashCondition, builder: Builder<ScriptBuilder>): IfChainBuilder =
        ifAll(conditions.toList(), builder)

    fun IfChainBuilder.ifAny(conditions: List<BashCondition>, builder: Builder<ScriptBuilder>): IfChainBuilder =
        if_(conditions, BashConditionStrategy.OR, builder = builder)

    fun IfChainBuilder.ifAny(vararg conditions: BashCondition, builder: Builder<ScriptBuilder>): IfChainBuilder =
        ifAny(conditions.toList(), builder)

    fun IfChainBuilder.if_(condition: BashCondition, builder: Builder<ScriptBuilder>): IfChainBuilder =
        if_(listOf(condition), builder = builder)

    fun IfChainBuilder.else_(builder: Builder<ScriptBuilder>): IfChainBuilder {
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

    private fun IfChainBuilder.if_(
        conditions: List<BashCondition>,
        strategy: BashConditionStrategy = BashConditionStrategy.AND,
        isElse: Boolean = false,
        builder: Builder<ScriptBuilder>
    ): IfChainBuilder {
        chain += Branch(conditions, strategy, isElse, script(builder = builder))
        return this
    }

    class Branch(
        val conditions: List<BashCondition>,
        val strategy: BashConditionStrategy,
        val isElse: Boolean,
        val body: String,
    )
}
