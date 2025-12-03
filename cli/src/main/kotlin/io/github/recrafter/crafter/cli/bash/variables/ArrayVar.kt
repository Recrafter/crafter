@file:Suppress("FunctionName")

package io.github.recrafter.crafter.cli.bash.variables

import io.github.diskria.kotlin.utils.Constants
import io.github.recrafter.crafter.cli.bash.references.VariableReference
import io.github.recrafter.crafter.cli.extensions.rounded
import io.github.recrafter.crafter.cli.extensions.squared

@JvmInline
value class ArrayVar(val name: String)

val ArrayVar.value: String
    get() = VariableReference.from(name)

fun ArrayVar.getElement(index: String): String =
    name + index.squared()

fun ArrayVar.getElement(index: Int): String =
    VariableReference.from(getElement(index.toString()))

fun ArrayVar.iterator_(): String =
    VariableReference.from(name + Constants.Char.AT_SIGN.toString().squared())

fun ArrayVar.add(element: String): String =
    "$name+=${element.rounded()}"

val ArrayVar.size: IntVar
    get() = IntVar("#${name}[@]")

fun ArrayVar.takeLast(count: Int): String =
    VariableReference.from("$name[@]: -$count")
