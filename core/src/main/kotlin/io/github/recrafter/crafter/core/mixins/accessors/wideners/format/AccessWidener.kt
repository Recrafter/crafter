package io.github.recrafter.crafter.core.mixins.accessors.wideners.format

import io.github.diskria.kotlin.utils.Constants

object AccessWidener : WidenerConfigFormat() {

    override val header: String = "accessWidener v2 named"

    override fun entryOf(binaryClassName: String): String =
        "accessible class ${binaryClassName.replace(Constants.Char.DOT, Constants.Char.SLASH)}"
}
