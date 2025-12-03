package io.github.recrafter.crafter.cli.bash.properties

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.common.AbstractAutoNamedProperty
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.variables.MapVar

class MapVarProperty(
    val builder: ScriptBuilder,
    val value: Map<String, String>
) : AbstractAutoNamedProperty<MapVar>() {

    override fun mapToValue(propertyName: String): MapVar =
        builder.initMap(propertyName.setCase(camelCase, SCREAMING_SNAKE_CASE), value)
}

fun ScriptBuilder.mapVar(value: Map<String, String>): MapVarProperty =
    MapVarProperty(this, value)
