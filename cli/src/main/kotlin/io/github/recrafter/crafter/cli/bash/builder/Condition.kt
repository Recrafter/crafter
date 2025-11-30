@file:Suppress("FunctionName")

package io.github.recrafter.crafter.cli.bash.builder

@JvmInline
value class Condition(val expression: String) {
    override fun toString(): String = expression
}

fun Condition.not_(): Condition =
    Condition("! $this")
