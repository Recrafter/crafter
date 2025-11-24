package io.github.recrafter.crafter.cli.properties

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.common.AbstractAutoNamedProperty
import io.github.recrafter.crafter.cli.bash.builder.BashScriptBuilder
import io.github.recrafter.crafter.cli.bash.builder.StringVar

class StringVarProperty(val builder: BashScriptBuilder, val value: String) : AbstractAutoNamedProperty<StringVar>() {

    override fun mapToValue(propertyName: String): StringVar =
        builder.initStringVar(propertyName.setCase(camelCase, SCREAMING_SNAKE_CASE), value)
}

fun BashScriptBuilder.stringVar(value: String): StringVarProperty =
    StringVarProperty(this, value)
