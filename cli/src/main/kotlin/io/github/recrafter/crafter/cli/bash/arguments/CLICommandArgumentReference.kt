package io.github.recrafter.crafter.cli.bash.arguments

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.recrafter.crafter.cli.bash.builder.BashScriptBuilder

class CLICommandArgumentReference private constructor(val name: String) {

    override fun toString(): String =
        BashScriptBuilder.getVariableReference(name)

    companion object {
        fun of(name: String): CLICommandArgumentReference =
            CLICommandArgumentReference(name.setCase(camelCase, SCREAMING_SNAKE_CASE))
    }
}
