package io.github.recrafter.crafter.cli.bash.properties

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.common.AbstractAutoNamedProperty
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.variables.BooleanVar

class BooleanVarProperty(
    val builder: ScriptBuilder,
    val value: Boolean,
) : AbstractAutoNamedProperty<BooleanVar>() {

    override fun mapToValue(propertyName: String): BooleanVar =
        builder.initBoolean(propertyName.setCase(camelCase, SCREAMING_SNAKE_CASE), value)
}

fun ScriptBuilder.booleanVar(value: Boolean = false): BooleanVarProperty =
    BooleanVarProperty(this, value)
