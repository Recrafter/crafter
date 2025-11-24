package io.github.recrafter.crafter.cli.bash.builder

enum class AnsiStyle {

    RESET,
    BOLD,
    DIM,
    UNDERLINE,
    BLINK,
    INVERT;

    val code: Int
        get() = when (this) {
            RESET -> 0
            BOLD -> 1
            DIM -> 2
            UNDERLINE -> 4
            BLINK -> 5
            INVERT -> 7
        }
}
