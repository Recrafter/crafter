@file:Suppress("FunctionName")

package io.github.recrafter.crafter.cli.bash.variables

import io.github.recrafter.crafter.cli.bash.conditions.BashCondition
import io.github.recrafter.crafter.cli.bash.references.VariableReference
import io.github.recrafter.crafter.cli.extensions.quoted
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class EnumVar<E : Enum<E>>(val name: String) {
    override fun toString(): String = value
}

val EnumVar<*>.value: String
    get() = VariableReference.from(name)

val EnumVar<*>.quotedValue: String
    get() = value.quoted()

fun <E : Enum<E>> EnumVar<E>.equals_(other: E): BashCondition =
    BashCondition.from("$quotedValue == ${other.name.lowercase().quoted()}")
