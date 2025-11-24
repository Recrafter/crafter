package io.github.recrafter.crafter.cli.properties

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.common.AbstractAutoNamedProperty
import io.github.recrafter.crafter.cli.bash.builder.BashScriptBuilder
import io.github.recrafter.crafter.cli.bash.builder.IntVar

class IntVarProperty(
    val builder: BashScriptBuilder,
    val value: String,
) : AbstractAutoNamedProperty<IntVar>() {

    override fun mapToValue(propertyName: String): IntVar =
        builder.initIntVar(propertyName.setCase(camelCase, SCREAMING_SNAKE_CASE), value)
}

fun BashScriptBuilder.intVar(value: String): IntVarProperty =
    IntVarProperty(this, value)

fun BashScriptBuilder.intVar(value: Int): IntVarProperty =
    intVar(value.toString())
