package io.github.recrafter.crafter.helpers.shell.syntax

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.diskria.kotlin.utils.extensions.primitives.wrapWithSpace
import io.github.recrafter.crafter.extensions.common.buildScript

class ShellCase internal constructor(
    val name: String,
    val aliases: List<String> = emptyList(),
    val body: String,
) {
    val script: String = buildScript {
        val condition = buildList {
            add(name)
            addAll(aliases)
        }.joinToString(Constants.Char.VERTICAL_BAR.wrapWithSpace())
        append { "$condition)" }
        withIndent {
            append { body }
        }
        append { Constants.Char.SEMICOLON.repeat(2) }
    }
}
