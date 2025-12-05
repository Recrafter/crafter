package io.github.recrafter.crafter.cli.bash

import io.github.diskria.kotlin.utils.extensions.mappers.getName

@Suppress("SpellCheckingInspection")
enum class BashCommand {

    DECLARE,
    SHOPT,
    ECHO, READ,
    CD, PWD,
    MKDIR, DIRNAME, BASENAME, REALPATH, CAT, TAIL, CP, MV, RM,
    SLEEP, KILL, WAIT, EXIT, TRAP,
    SOURCE,
    DATE,
    MKFIFO, EXEC,
    COMPGEN, COMPLETE;

    val command: String get() = getName()

    override fun toString(): String = command
}
