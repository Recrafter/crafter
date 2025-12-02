@file:Suppress("FunctionName")

package io.github.recrafter.crafter.cli.bash.builder

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.wrapWithSpace
import io.github.recrafter.crafter.cli.extensions.curled
import io.github.recrafter.crafter.cli.extensions.squared

@JvmInline
value class BooleanVar(val name: String) {
    override fun toString(): String = value

    companion object {
        fun from(boolean: Boolean): Int =
            if (boolean) 0 else 1
    }
}

val BooleanVar.value: String
    get() = buildString {
        append(Constants.Char.DOLLAR)
        append(name.curled())
    }

fun BooleanVar.equals_(other: Boolean): Condition =
    Condition("$value -eq ${BooleanVar.from(other)}".wrapWithSpace().squared(2))
