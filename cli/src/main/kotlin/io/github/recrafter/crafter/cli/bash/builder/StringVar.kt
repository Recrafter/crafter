@file:Suppress("FunctionName")

package io.github.recrafter.crafter.cli.bash.builder

import io.github.diskria.kotlin.utils.Constants
import io.github.recrafter.crafter.cli.extensions.curled
import io.github.recrafter.crafter.cli.extensions.quoted

@JvmInline
value class StringVar(val name: String) {
    override fun toString(): String = value
}

val StringVar.value: String
    get() = getValue()

val StringVar.quotedValue: String
    get() = value.quoted()

val StringVar.length: String
    get() = buildValue(Constants.Char.NUMBER_SIGN + name)

fun StringVar.getValue(default: String? = null): String =
    buildValue(
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

fun StringVar.equals_(other: String): Condition =
    Condition("$quotedValue == $other")

fun StringVar.equals_(other: StringVar): Condition =
    equals_(other.value)

fun StringVar.getCharAt(index: String): String =
    buildValue("${name}:$index:1")

fun StringVar.substring(startIndex: Int, endIndex: String): String =
    buildValue("${name}:$startIndex:$endIndex")

fun StringVar.isVarNotEmpty(): Condition =
    Condition("-n $quotedValue")

private fun buildValue(text: String): String =
    buildString {
        append(Constants.Char.DOLLAR)
        append(text.curled())
    }
