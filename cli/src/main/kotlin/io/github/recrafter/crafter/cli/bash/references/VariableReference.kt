package io.github.recrafter.crafter.cli.bash.references

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.recrafter.crafter.cli.extensions.curled
import io.github.recrafter.crafter.cli.extensions.quoted

class VariableReference private constructor(val name: String) {

    override fun toString(): String =
        from(name).quoted()

    companion object {
        fun of(name: String): VariableReference =
            VariableReference(name.setCase(camelCase, SCREAMING_SNAKE_CASE))

        fun from(expression: String): String =
            Constants.Char.DOLLAR + expression.curled()
    }
}
