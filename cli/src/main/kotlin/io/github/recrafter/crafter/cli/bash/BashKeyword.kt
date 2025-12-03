package io.github.recrafter.crafter.cli.bash

import io.github.diskria.kotlin.utils.extensions.mappers.getName

enum class BashKeyword {

    IF, THEN, ELIF, ELSE, FI,
    CASE, IN, ESAC,
    FOR, WHILE, DO, DONE,
    CONTINUE, BREAK,
    RETURN;

    val token: String get() = getName()

    override fun toString(): String = token
}
