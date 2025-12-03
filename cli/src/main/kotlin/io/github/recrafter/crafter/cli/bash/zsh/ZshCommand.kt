package io.github.recrafter.crafter.cli.bash.zsh

import io.github.diskria.kotlin.utils.extensions.mappers.getName

enum class ZshCommand {

    AUTOLOAD,
    BASHCOMPINIT;

    val command: String get() = getName()

    override fun toString(): String = command
}
