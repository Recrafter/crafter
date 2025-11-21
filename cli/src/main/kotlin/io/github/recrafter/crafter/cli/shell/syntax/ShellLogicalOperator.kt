package io.github.recrafter.crafter.cli.shell.syntax

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.diskria.kotlin.utils.extensions.wrapWithSpace

enum class ShellLogicalOperator(char: Char) {

    AND(Constants.Char.AMPERSAND),
    OR(Constants.Char.VERTICAL_BAR);

    val token: String =
        char.repeat(2).wrapWithSpace()
}
