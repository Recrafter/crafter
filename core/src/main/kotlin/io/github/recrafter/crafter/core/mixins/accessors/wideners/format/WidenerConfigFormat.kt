package io.github.recrafter.crafter.core.mixins.accessors.wideners.format

sealed class WidenerConfigFormat {

    open val header: String? = null

    abstract fun entryOf(binaryClassName: String): String
}
