package io.github.recrafter.crafter.core.versions.range

import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.buildString
import io.github.diskria.kotlin.utils.extensions.wrapWithBrackets
import io.github.recrafter.crafter.core.versions.VersionBound

object IntervalVersionRange : VersionRange() {

    private const val SEPARATOR: Char = Constants.Char.COMMA
    private val EXCLUSIVE_BRACKETS_TYPE: BracketsType = BracketsType.ROUND

    override val any: String =
        SEPARATOR.toString().wrapWithBrackets(EXCLUSIVE_BRACKETS_TYPE)

    override fun rangeInternal(minVersion: VersionBound?, maxVersion: VersionBound?): String {
        val min = when (minVersion) {
            null -> EXCLUSIVE_BRACKETS_TYPE.openingChar.toString()
            else -> buildString {
                val bracketsType = getBracketsType(minVersion.isInclusive())
                append(bracketsType.openingChar)
                append(minVersion)
            }
        }
        val max = when (maxVersion) {
            null -> buildString(EXCLUSIVE_BRACKETS_TYPE.closingChar)
            else -> buildString {
                append(maxVersion.toString())
                append(getBracketsType(maxVersion.isInclusive()).closingChar)
            }
        }
        return min + SEPARATOR + max
    }

    private fun getBracketsType(isInclusive: Boolean): BracketsType =
        if (isInclusive) BracketsType.SQUARE
        else EXCLUSIVE_BRACKETS_TYPE
}
