package io.github.recrafter.crafter.cli.bash.properties

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.common.AbstractAutoNamedProperty
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.variables.ArrayVar
import io.github.recrafter.crafter.cli.bash.variables.StringVar
import io.github.recrafter.crafter.cli.bash.variables.value

class ArrayVarProperty(val builder: ScriptBuilder, val value: String?) : AbstractAutoNamedProperty<ArrayVar>() {

    override fun mapToValue(propertyName: String): ArrayVar =
        builder.initArray(propertyName.setCase(camelCase, SCREAMING_SNAKE_CASE), value)
}

fun ScriptBuilder.arrayVar(value: String? = null): ArrayVarProperty =
    ArrayVarProperty(this, value)

fun ScriptBuilder.arrayVar(value: StringVar): ArrayVarProperty =
    arrayVar(value.value)
