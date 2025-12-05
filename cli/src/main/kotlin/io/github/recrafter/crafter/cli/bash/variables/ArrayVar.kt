@file:Suppress("FunctionName")

package io.github.recrafter.crafter.cli.bash.variables

import io.github.diskria.kotlin.utils.Constants
import io.github.recrafter.crafter.cli.bash.conditions.BashCondition
import io.github.recrafter.crafter.cli.bash.conditions.not_
import io.github.recrafter.crafter.cli.bash.references.VariableReference
import io.github.recrafter.crafter.cli.extensions.rounded
import io.github.recrafter.crafter.cli.extensions.squared

@JvmInline
value class ArrayVar(val name: String)

val ArrayVar.value: String
    get() = VariableReference.from(name)

fun ArrayVar.getElement(index: String): String =
    VariableReference.from(name + index.squared())

fun ArrayVar.getElement(index: Int): String =
    VariableReference.from(name + index.toString().squared())

fun ArrayVar.getElement(index: IntVar): String =
    getElement(index.value)

fun ArrayVar.iterator_(): String =
    VariableReference.from(elements)

fun ArrayVar.add(element: String): String =
    "$name+=${element.rounded()}"

fun ArrayVar.isEmpty(): BashCondition =
    size.equals_(0)

fun ArrayVar.isNotEmpty(): BashCondition =
    isEmpty().not_()

val ArrayVar.size: IntVar
    get() = IntVar("#$elements")

fun ArrayVar.takeLast(count: Int): String =
    VariableReference.from("$elements: -$count")

private val ArrayVar.elements: String
    get() = name + Constants.Char.AT_SIGN.toString().squared()
