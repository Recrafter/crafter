package io.github.recrafter.crafter.cli.bash.properties

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.common.AbstractAutoNamedProperty
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.variables.StringVar

class StringVarProperty(val builder: ScriptBuilder, val value: String) : AbstractAutoNamedProperty<StringVar>() {

    override fun mapToValue(propertyName: String): StringVar =
        builder.initString(propertyName.setCase(camelCase, SCREAMING_SNAKE_CASE), value)
}

fun ScriptBuilder.stringVar(value: String): StringVarProperty =
    StringVarProperty(this, value)

fun ScriptBuilder.stringVar(): StringVarProperty =
    stringVar(Constants.Char.EMPTY)
