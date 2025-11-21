package io.github.recrafter.crafter.helpers.shell.syntax

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.primitives.repeat
import io.github.diskria.kotlin.utils.extensions.primitives.wrapWithSpace
import io.github.recrafter.crafter.extensions.common.shellScript
import io.github.recrafter.crafter.helpers.shell.ShellScriptBuilder

class ShellCase internal constructor(
    val name: String,
    val aliases: List<String> = emptyList(),
    val body: String,
) {
    val script: String = shellScript {
        val condition = buildList {
            add(name)
            addAll(aliases)
        }.joinToString(Constants.Char.VERTICAL_BAR.wrapWithSpace())
        code { "$condition)" }
        withIndent {
            code { body }
        }
        code { Constants.Char.SEMICOLON.repeat(2) }
    }

    override fun toString(): String =
        script

    companion object {
        fun of(
            name: String,
            aliases: List<String> = emptyList(),
            builder: ShellScriptBuilder.() -> ShellScriptBuilder
        ): ShellCase =
            ShellCase(name, aliases, shellScript(builder))
    }
}
