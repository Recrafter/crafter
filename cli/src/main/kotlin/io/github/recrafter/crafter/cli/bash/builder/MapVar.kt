package io.github.recrafter.crafter.cli.bash.builder

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.wrapWithSpace
import io.github.recrafter.crafter.cli.extensions.curled
import io.github.recrafter.crafter.cli.extensions.quoted
import io.github.recrafter.crafter.cli.extensions.squared
import io.github.recrafter.crafter.cli.extensions.unquoted

@JvmInline
value class MapVar(val name: String)

val MapVar.value: String
    get() = buildValue(name)

val MapVar.quotedValue: String
    get() = value.quoted()

fun MapVar.getValue(key: String): String =
    buildValue(name + key.unquoted().squared()).quoted()

fun MapVar.containsKey(key: String): Condition =
    Condition("-v $name${key.quoted().squared()}".wrapWithSpace().squared(2))

private fun buildValue(text: String): String =
    buildString {
        append(Constants.Char.DOLLAR)
        append(text.curled())
    }
