package io.github.recrafter.crafter.cli.bash.conditions

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.primitives.repeat

enum class BashConditionStrategy(char: Char) {

    AND(Constants.Char.AMPERSAND),
    OR(Constants.Char.VERTICAL_BAR);

    val operator: String = char.repeat(2)
}
