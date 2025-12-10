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

fun IntVar.decrement(): String =
    arithmetic("$name--")

fun IntVar.mod(other: IntVar): String =
    arithmetic("$value % ${other.name}")

fun IntVar.minus(other: Int): String =
    "$value-$other"

fun IntVar.equals_(other: String): BashCondition =
    BashCondition.from("$value -eq $other")

fun IntVar.equals_(other: Int): BashCondition =
    equals_(other.toString())

fun IntVar.equals_(other: IntVar): BashCondition =
    equals_(other.value)

fun IntVar.isGreaterThen(other: Int): BashCondition =
    BashCondition.from("$value -gt $other")

fun IntVar.isGreaterOrEqual(other: Int): BashCondition =
    BashCondition.from("$value -ge $other")

fun IntVar.isEmpty(): BashCondition =
    BashCondition.from("-z $value")

fun IntVar.isNotEmpty(): BashCondition =
    BashCondition.from("-n $value")

fun IntVar.isLessThen(other: String): BashCondition =
    BashCondition.from("$value -lt $other")

fun IntVar.isLessThen(other: IntVar): BashCondition =
    isLessThen(other.value)

fun IntVar.isLessThen(other: Int): BashCondition =
    isLessThen(other.toString())

fun IntVar.isLessOrEqual(other: String): BashCondition =
    BashCondition.from("$value -le $other")

fun IntVar.isLessOrEqual(other: IntVar): BashCondition =
    isLessOrEqual(other.value)

fun IntVar.isLessOrEqual(other: Int): BashCondition =
    isLessOrEqual(other.toString())

fun String.toIntVar(): IntVar =
    IntVar(this)

private fun arithmetic(expression: String): String =
    expression.rounded(2)
