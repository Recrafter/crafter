package io.github.recrafter.crafter.core.versions.range

import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.wrapWithBrackets
import io.github.recrafter.crafter.core.versions.VersionBound

object IntervalVersionRange : VersionRange() {

    private const val SEPARATOR: Char = Constants.Char.COMMA
    private val EXCLUSIVE_BRACKETS_TYPE: BracketsType = BracketsType.ROUND

    override val any: String =
        SEPARATOR.toString().wrapWithBrackets(EXCLUSIVE_BRACKETS_TYPE)

    override fun rangeInternal(minVersion: VersionBound?, maxVersion: VersionBound?): String {
        val min = buildString {
            if (minVersion == null) {
                append(EXCLUSIVE_BRACKETS_TYPE.openingChar)
            } else {
                val bracketsType = getBracketsType(minVersion.isInclusive())
                append(bracketsType.openingChar)
                append(minVersion)
            }
        }
        val max = buildString {
            if (maxVersion == null) {
                append(EXCLUSIVE_BRACKETS_TYPE.closingChar)
            } else {
                append(maxVersion)
                append(getBracketsType(maxVersion.isInclusive()).closingChar)
            }
        }
        return min + SEPARATOR + max
    }

    private fun getBracketsType(isInclusive: Boolean): BracketsType =
        if (isInclusive) BracketsType.SQUARE
        else EXCLUSIVE_BRACKETS_TYPE
}
