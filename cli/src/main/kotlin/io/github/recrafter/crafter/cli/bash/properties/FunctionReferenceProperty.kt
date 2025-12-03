package io.github.recrafter.crafter.cli.bash.properties

import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.common.snake_case
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.properties.common.AbstractAutoNamedProperty
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.references.FunctionReference
import io.github.recrafter.crafter.cli.extensions.common.Builder

class FunctionReferenceProperty(
    val builder: ScriptBuilder,
    val isPrivate: Boolean,
    val body: Builder<ScriptBuilder>
) : AbstractAutoNamedProperty<FunctionReference>() {

    override fun mapToValue(propertyName: String): FunctionReference =
        builder.initFunction(
            buildString {
                if (isPrivate) {
                    append("_")
                }
                append(propertyName.removeSuffix("Function").setCase(camelCase, snake_case))
            },
            body
        )
}

fun ScriptBuilder.function(isPrivate: Boolean = false, body: Builder<ScriptBuilder>): FunctionReferenceProperty =
    FunctionReferenceProperty(this, isPrivate, body)
