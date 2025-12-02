package io.github.recrafter.crafter.cli.bash.syntax

import io.github.diskria.kotlin.utils.extensions.mappers.getName

enum class BashKeyword {

    CASE,
    IN,
    IF,
    THEN,
    ELIF,
    ELSE,
    BREAK,
    CONTINUE,
    RETURN;

    val token: String
        get() = getName()

    val closingToken: String
        get() = token.reversed()
}
