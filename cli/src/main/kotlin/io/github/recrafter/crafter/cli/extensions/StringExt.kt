package io.github.recrafter.crafter.cli.extensions

import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.wrapWithBrackets
import io.github.diskria.kotlin.utils.extensions.wrapWithDoubleQuote
import io.github.diskria.kotlin.utils.extensions.wrapWithSingleQuote

fun String.unquoted(): String =
    trimStart(Constants.Char.DOUBLE_QUOTE).trimEnd(Constants.Char.DOUBLE_QUOTE)

fun String.quoted(): String =
    unquoted().wrapWithDoubleQuote()

fun String.singleQuoted(): String =
    unquoted().wrapWithSingleQuote()

fun String.squared(count: Int = 1): String =
    wrapWithBrackets(BracketsType.SQUARE, count)

fun String.rounded(count: Int = 1): String =
    wrapWithBrackets(BracketsType.ROUND, count)

fun String.angled(count: Int = 1): String =
    wrapWithBrackets(BracketsType.ANGLE, count)

fun String.curled(): String =
    wrapWithBrackets(BracketsType.CURLY)
