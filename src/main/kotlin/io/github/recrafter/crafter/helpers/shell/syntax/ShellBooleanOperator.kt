package io.github.recrafter.crafter.helpers.shell.syntax

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.diskria.kotlin.utils.extensions.wrapWithSpace

enum class ShellBooleanOperator(char: Char) {

    AND('&'),
    OR(Constants.Char.VERTICAL_BAR);

    val token: String =
        char.repeat(2).wrapWithSpace()
}
