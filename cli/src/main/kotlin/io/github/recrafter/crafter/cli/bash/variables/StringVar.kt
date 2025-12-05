@file:Suppress("FunctionName")

package io.github.recrafter.crafter.cli.bash.variables

import io.github.diskria.kotlin.utils.Constants
import io.github.recrafter.crafter.cli.bash.conditions.BashCondition
import io.github.recrafter.crafter.cli.bash.references.VariableReference
import io.github.recrafter.crafter.cli.extensions.quoted
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class StringVar(val name: String) {
    override fun toString(): String = value
}

val StringVar.value: String
    get() = getValue()

val StringVar.quotedValue: String
    get() = value.quoted()

val StringVar.length: String
    get() = VariableReference.from(Constants.Char.NUMBER_SIGN + name)

fun StringVar.getValue(default: String? = null): String =
    VariableReference.from(
        buildString {
            append(name)
            default?.let {
                append(Constants.Char.HYPHEN)
                append(it)
            }
        }
    )

fun StringVar.getQuotedValue(default: String? = null): String =
    getValue(default).quoted()

fun StringVar.equals_(other: String): BashCondition =
    BashCondition.from("$quotedValue == ${other.quoted()}")

fun StringVar.equals_(other: StringVar): BashCondition =
    equals_(other.value)

fun StringVar.substring(startIndex: String, length: String): String =
    VariableReference.from("${name}:$startIndex:$length")

fun StringVar.substring(startIndex: String, length: Int): String =
    substring(startIndex, length.toString())

fun StringVar.substring(startIndex: Int, length: Int): String =
    substring(startIndex.toString(), length.toString())

fun StringVar.getCharAt(index: String): String =
    substring(index, 1)

fun StringVar.startsWith(prefix: String): BashCondition =
    BashCondition.from("$quotedValue == $prefix*")

fun StringVar.startsWith(prefix: StringVar): BashCondition =
    startsWith(prefix.value)

fun StringVar.endsWith(suffix: String): BashCondition =
    BashCondition.from("$quotedValue == *$suffix")

fun StringVar.endsWith(suffix: StringVar): BashCondition =
    endsWith(suffix.value)

fun StringVar.removePrefix(prefix: String): String =
    VariableReference.from("$name#$prefix")

fun StringVar.removePrefix(prefix: StringVar): String =
    removePrefix(prefix.quotedValue)

fun StringVar.removeSuffix(suffix: String): String =
    VariableReference.from("$name%$suffix")

fun StringVar.removeSuffix(suffix: StringVar): String =
    removePrefix(suffix.quotedValue)

fun StringVar.substringAfterLast(delimiter: Char): String =
    VariableReference.from("$name##*$delimiter")

fun StringVar.isEmpty(): BashCondition =
    BashCondition.from("-z $quotedValue")

fun StringVar.isNotEmpty(): BashCondition =
    BashCondition.from("-n $quotedValue")

fun StringVar.contains(other: String): BashCondition =
    BashCondition.from("echo $quotedValue | grep -Fq ${other.quoted()}", squared = false)

fun StringVar.matches(regex: String): BashCondition =
    BashCondition.from("$quotedValue =~ $regex")

fun StringVar.split(delimiter: String): String =
    VariableReference.from("$name//$delimiter/${Constants.Char.SPACE}")
