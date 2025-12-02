package io.github.recrafter.crafter.cli.properties

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.common.AbstractAutoNamedProperty
import io.github.recrafter.crafter.cli.bash.builder.BashScriptBuilder
import io.github.recrafter.crafter.cli.bash.builder.BooleanVar

class BooleanVarProperty(
    val builder: BashScriptBuilder,
    val value: Boolean,
) : AbstractAutoNamedProperty<BooleanVar>() {

    override fun mapToValue(propertyName: String): BooleanVar =
        builder.initBooleanVar(propertyName.setCase(camelCase, SCREAMING_SNAKE_CASE), value)
}

fun BashScriptBuilder.booleanVar(value: Boolean = false): BooleanVarProperty =
    BooleanVarProperty(this, value)
