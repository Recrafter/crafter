package io.github.recrafter.crafter.cli.properties

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.common.AbstractAutoNamedProperty
import io.github.recrafter.crafter.cli.bash.builder.ArrayVar
import io.github.recrafter.crafter.cli.bash.builder.BashScriptBuilder
import io.github.recrafter.crafter.cli.bash.builder.StringVar

class ArrayVarProperty(val builder: BashScriptBuilder, val value: String) : AbstractAutoNamedProperty<ArrayVar>() {

    override fun mapToValue(propertyName: String): ArrayVar =
        builder.initArrayVar(propertyName.setCase(camelCase, SCREAMING_SNAKE_CASE), value)
}

fun BashScriptBuilder.arrayVar(value: String): ArrayVarProperty =
    ArrayVarProperty(this, value)
