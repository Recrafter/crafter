@file:Suppress("FunctionName")

package io.github.recrafter.crafter.cli.bash.variables

import io.github.recrafter.crafter.cli.bash.conditions.BashCondition
import io.github.recrafter.crafter.cli.bash.references.VariableReference
import io.github.recrafter.crafter.cli.extensions.rounded

@JvmInline
value class IntVar(val name: String) {
    override fun toString(): String = value
}

val IntVar.value: String
    get() = VariableReference.from(name)

fun IntVar.increment(): String =
    arithmetic("$name++")

fun IntVar.mod(other: IntVar): String =
    arithmetic("$name % ${other.name}")

fun IntVar.minus(other: Int): String =
    arithmetic("$name-$other")

fun IntVar.equals_(other: String): BashCondition =
    BashCondition.from("$value -eq $other")

fun IntVar.equals_(other: Int): BashCondition =
    equals_(other.toString())

fun IntVar.equals_(other: IntVar): BashCondition =
    equals_(other.value)

fun IntVar.isGreaterThen(other: Int): BashCondition =
    BashCondition.from("$value -gt $other")

fun IntVar.isEmpty(): BashCondition =
    BashCondition.from("-z $value")

fun IntVar.isNotEmpty(): BashCondition =
    BashCondition.from("-n $value")

fun IntVar.isLessOrEqual(other: IntVar): BashCondition =
    BashCondition.from("$value -le $other")

fun String.toIntVar(): IntVar =
    IntVar(this)

private fun arithmetic(expression: String): String =
    expression.rounded(2)
