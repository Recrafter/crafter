package io.github.recrafter.crafter.cli.bash.variables

import io.github.recrafter.crafter.cli.bash.conditions.BashCondition
import io.github.recrafter.crafter.cli.bash.references.VariableReference
import io.github.recrafter.crafter.cli.extensions.quoted
import io.github.recrafter.crafter.cli.extensions.squared
import io.github.recrafter.crafter.cli.extensions.unquoted

@JvmInline
value class MapVar(val name: String)

val MapVar.value: String
    get() = VariableReference.from(name)

val MapVar.quotedValue: String
    get() = value.quoted()

fun MapVar.getValue(key: String): String =
    VariableReference.from(name + key.unquoted().squared()).quoted()

fun MapVar.getValue(key: StringVar): String =
    getValue(key.toString())

fun MapVar.containsKey(key: String): BashCondition =
    BashCondition.from("-v $name${key.quoted().squared()}")
