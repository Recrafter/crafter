@file:Suppress("FunctionName")

package io.github.recrafter.crafter.cli.bash.conditions

import io.github.diskria.kotlin.utils.extensions.common.modifyIf
import io.github.diskria.kotlin.utils.extensions.wrapWithSpace
import io.github.recrafter.crafter.cli.extensions.squared

class BashCondition private constructor(val expression: String) {

    override fun toString(): String = expression

    companion object {
        fun from(expression: String, squared: Boolean = true): BashCondition =
            BashCondition(expression.modifyIf(squared) { it.wrapWithSpace().squared(2) })
    }
}

fun BashCondition.not_(): BashCondition =
    BashCondition.from("! $this", squared = false)
