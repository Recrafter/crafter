@file:Suppress("FunctionName")

package io.github.recrafter.crafter.cli.bash.variables

import io.github.recrafter.crafter.cli.bash.conditions.BashCondition
import io.github.recrafter.crafter.cli.bash.references.VariableReference

@JvmInline
value class BooleanVar(val name: String) {
    override fun toString(): String = value

    companion object {
        fun from(boolean: Boolean): Int =
            if (boolean) 0 else 1
    }
}

val BooleanVar.value: String
    get() = VariableReference.from(name)

fun BooleanVar.equals_(other: Boolean): BashCondition =
    BashCondition.from("$value -eq ${BooleanVar.from(other)}")
