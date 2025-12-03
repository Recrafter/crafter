package io.github.recrafter.crafter.cli.bash.api.commands.arguments.enums

import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.camelCase
import io.github.diskria.kotlin.utils.extensions.setCase

interface CLIArgumentEnum<E> where E : Enum<E>, E : CLIArgumentEnum<E> {

    val name: String
    val description: String

    val defaultEnum: E?
        get() = null

    val argumentName: String
        get() = name.setCase(SCREAMING_SNAKE_CASE, camelCase)
}
