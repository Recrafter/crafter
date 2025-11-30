@file:Suppress("FunctionName")

package io.github.recrafter.crafter.cli.bash.builder

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.wrapWithSpace
import io.github.recrafter.crafter.cli.extensions.curled
import io.github.recrafter.crafter.cli.extensions.rounded
import io.github.recrafter.crafter.cli.extensions.squared

@JvmInline
value class IntVar(val name: String) {
    override fun toString(): String = value
}

val IntVar.value: String
    get() = buildString {
        append(Constants.Char.DOLLAR)
        append(name.curled())
    }

fun IntVar.increment(): String =
    arithmetic("$name++")

fun IntVar.mod(other: IntVar): String =
    arithmetic("$name % ${other.name}")

fun IntVar.minus(other: Int): String =
    arithmetic("$name-$other")

fun IntVar.equals_(other: Int): Condition =
    Condition("$value -eq $other".wrapWithSpace().squared(2))

fun IntVar.isGreaterThen(other: Int): Condition =
    Condition("$value -gt $other".wrapWithSpace().squared(2))

fun String.toIntVar(): IntVar =
    IntVar(this)

private fun arithmetic(expression: String): String =
    expression.rounded(2)
