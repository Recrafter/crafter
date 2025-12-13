package io.github.recrafter.crafter.core.versions.range

import io.github.diskria.kotlin.utils.extensions.common.failWithWrongUsage
import io.github.recrafter.crafter.core.versions.VersionBound

sealed class VersionRange {

    abstract val any: String

    protected abstract fun rangeInternal(
        minVersion: VersionBound?,
        maxVersion: VersionBound?
    ): String

    fun min(version: VersionBound): String =
        rangeInternal(version, null)

    fun max(version: VersionBound): String =
        rangeInternal(null, version)

    fun range(minVersion: VersionBound? = null, maxVersion: VersionBound? = null): String {
        if (minVersion == null && maxVersion == null) {
            failWithWrongEmptyRangeUsage()
        }
        return rangeInternal(minVersion, maxVersion)
    }

    protected fun failWithWrongEmptyRangeUsage(): Nothing =
        failWithWrongUsage(useInsteadThis = ::any.name)
}
