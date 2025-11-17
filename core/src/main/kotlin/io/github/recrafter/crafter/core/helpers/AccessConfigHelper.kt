package io.github.recrafter.crafter.core.helpers

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.generics.joinByNewLine
import io.github.diskria.kotlin.utils.extensions.generics.toNullIfEmpty
import io.github.diskria.kotlin.utils.extensions.toNullIfEmpty
import java.io.File

object AccessConfigHelper {

    val WIDENER_PRESET: String by lazy {
        buildString {
            appendLine("accessWidener v2 named")
            appendLine()
            appendLine("# region class")
            appendLine()
            appendLine("# endregion class")
            appendLine()
            appendLine("# region method")
            appendLine()
            appendLine("# endregion method")
            appendLine()
            appendLine("# region field")
            appendLine()
            appendLine("# endregion field")
        }
    }

    fun mergeConfigs(configs: Iterable<File>): String =
        configs
            .mapNotNull { config ->
                config
                    .readLines()
                    .mapNotNull { line -> line.trim().toNullIfEmpty() }
                    .filterNot { it.startsWith(Constants.Char.NUMBER_SIGN) }
                    .toNullIfEmpty()
            }
            .flatten()
            .toSet()
            .joinByNewLine()
}
