package io.github.recrafter.crafter.cli.ascii

object BoxDraw {

    const val HORIZONTAL: Char = '─'
    const val VERTICAL: Char = '│'

    object Corner {
        const val TOP_LEFT: Char = '┌'
        const val TOP_RIGHT: Char = '┐'
        const val BOTTOM_RIGHT: Char = '┘'
        const val BOTTOM_LEFT: Char = '└'
    }

    object Connect {
        const val LEFT: Char = '├'
        const val TOP: Char = '┬'
        const val RIGHT: Char = '┤'
        const val BOTTOM: Char = '┴'
        const val CENTER: Char = '┼'
    }
}