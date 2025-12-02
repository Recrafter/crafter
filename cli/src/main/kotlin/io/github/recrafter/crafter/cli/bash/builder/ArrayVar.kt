@file:Suppress("FunctionName")

package io.github.recrafter.crafter.cli.bash.builder

import io.github.diskria.kotlin.utils.Constants
import io.github.recrafter.crafter.cli.extensions.curled
import io.github.recrafter.crafter.cli.extensions.squared

@JvmInline
value class ArrayVar(val name: String)

val ArrayVar.value: String
    get() = buildValue(name)

fun ArrayVar.getElement(index: String): String =
    name + index.squared()

fun ArrayVar.getElement(index: Int): String =
    buildValue(getElement(index.toString()))

fun ArrayVar.iterator_(): String =
    buildValue(name + Constants.Char.AT_SIGN.toString().squared())

fun ArrayVar.add(element: String): String =
    "$name+=($element)"

val ArrayVar.size: IntVar
    get() = IntVar("#${name}[@]")

fun ArrayVar.takeLast(count: Int): String =
    "\${$name[@]: -$count}"

private fun buildValue(text: String): String =
    buildString {
        append(Constants.Char.DOLLAR)
        append(text.curled())
    }
