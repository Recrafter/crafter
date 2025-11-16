package io.github.recrafter.crafter.versions.range

import io.github.diskria.kotlin.utils.Constants
import io.github.recrafter.crafter.versions.VersionBound

object InequalityVersionRange : VersionRange() {

    override val any: String = Constants.Char.ASTERISK.toString()

    override fun rangeInternal(minVersion: VersionBound?, maxVersion: VersionBound?): String {
        val min = minVersion?.let {
            buildString {
                append(Constants.Char.CLOSING_ANGLE_BRACKET)
                if (minVersion.isInclusive()) {
                    append(Constants.Char.EQUAL_SIGN)
                }
                append(minVersion.toString())
            }
        }
        val max = maxVersion?.let {
            buildString {
                append(Constants.Char.OPENING_ANGLE_BRACKET)
                if (maxVersion.isInclusive()) {
                    append(Constants.Char.EQUAL_SIGN)
                }
                append(maxVersion.toString())
            }
        }
        return when {
            min != null && max != null -> min + Constants.Char.SPACE + max
            min != null -> min
            max != null -> max
            else -> useAnyInsteadEmptyRange()
        }
    }
}
