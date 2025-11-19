package io.github.recrafter.crafter.helpers.shell.syntax

import io.github.diskria.kotlin.utils.extensions.mappers.getName

enum class ShellKeyword() {

    CASE,
    IF;

    val token: String
        get() = getName()

    val end: String
        get() = token.reversed()
}
