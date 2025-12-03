package io.github.recrafter.crafter.cli.bash.ansi

object AnsiCodes {

    const val ESCAPE: String = """\033["""

    fun escape(code: String): String =
        ESCAPE + code
}
