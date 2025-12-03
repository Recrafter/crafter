package io.github.recrafter.crafter.cli.bash.properties

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.common.AbstractAutoNamedProperty
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.variables.IntVar

class IntVarProperty(
    val builder: ScriptBuilder,
    val value: String,
) : AbstractAutoNamedProperty<IntVar>() {

    override fun mapToValue(propertyName: String): IntVar =
        builder.initInt(propertyName.setCase(camelCase, SCREAMING_SNAKE_CASE), value)
}

fun ScriptBuilder.intVar(value: String): IntVarProperty =
    IntVarProperty(this, value)

fun ScriptBuilder.intVar(value: Int = 0): IntVarProperty =
    intVar(value.toString())
