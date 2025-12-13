package io.github.recrafter.crafter.core.mixins.accessors.wideners.format

import io.github.diskria.kotlin.utils.Constants

object AccessTransformer : WidenerConfigFormat() {

    override fun entryOf(binaryClassName: String): String =
        "public-f ${binaryClassName.replace(Constants.Char.SLASH, Constants.Char.DOT)}"
}
