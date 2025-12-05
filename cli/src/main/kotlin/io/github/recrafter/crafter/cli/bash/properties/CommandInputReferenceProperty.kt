package io.github.recrafter.crafter.cli.bash.properties

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.common.AbstractAutoNamedProperty
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder

class CommandInputReferenceProperty(
    val builder: ScriptBuilder,
) : AbstractAutoNamedProperty<CommandInputReference>() {

    override fun mapToValue(propertyName: String): CommandInputReference =
        builder.initCommandInput(propertyName.setCase(camelCase, SCREAMING_SNAKE_CASE))
}

fun ScriptBuilder.commandInput(): CommandInputReferenceProperty =
    CommandInputReferenceProperty(this)
