package io.github.recrafter.crafter.helpers.shell

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendFollowingIndent

class ScriptBuilder {

    private var script: String = Constants.Char.EMPTY
    private var offset: Int = 0

    fun ScriptBuilder.append(code: () -> String): ScriptBuilder {
        script = script.appendFollowingIndent(code(), offset).trimIndent()
        return this
    }

    fun ScriptBuilder.indentIn(steps: Int = DEFAULT_INDENT) {
        offset = steps
    }

    fun ScriptBuilder.indentOut(steps: Int = DEFAULT_INDENT) {
        offset = -steps
    }

    fun ScriptBuilder.withIndent(steps: Int = DEFAULT_INDENT, builder: ScriptBuilder.() -> Unit) {
        indentIn(steps)
        apply(builder)
        indentOut(steps)
    }

    override fun toString(): String =
        script

    companion object {
        private const val DEFAULT_INDENT: Int = 4
    }
}
