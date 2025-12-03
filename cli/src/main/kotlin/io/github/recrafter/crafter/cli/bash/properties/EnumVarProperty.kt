package io.github.recrafter.crafter.cli.bash.properties

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.common.AbstractAutoNamedProperty
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.variables.EnumVar

class EnumVarProperty<E : Enum<E>>(
    val builder: ScriptBuilder,
    val value: E?,
) : AbstractAutoNamedProperty<EnumVar<E>>() {

    override fun mapToValue(propertyName: String): EnumVar<E> =
        builder.initEnum(propertyName.setCase(camelCase, SCREAMING_SNAKE_CASE), value)
}

fun <E : Enum<E>> ScriptBuilder.enumVar(value: E? = null): EnumVarProperty<E> =
    EnumVarProperty(this, value)
