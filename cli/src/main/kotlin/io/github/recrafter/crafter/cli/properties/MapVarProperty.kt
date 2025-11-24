package io.github.recrafter.crafter.cli.properties

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.common.AbstractAutoNamedProperty
import io.github.recrafter.crafter.cli.bash.builder.BashScriptBuilder
import io.github.recrafter.crafter.cli.bash.builder.MapVar

class MapVarProperty(
    val builder: BashScriptBuilder,
    val value: Map<String, String>
) : AbstractAutoNamedProperty<MapVar>() {

    override fun mapToValue(propertyName: String): MapVar =
        builder.initMapVar(propertyName.setCase(camelCase, SCREAMING_SNAKE_CASE), value)
}

fun BashScriptBuilder.mapVar(value: Map<String, String>): MapVarProperty =
    MapVarProperty(this, value)
