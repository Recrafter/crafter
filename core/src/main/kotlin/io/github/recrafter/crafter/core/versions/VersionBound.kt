package io.github.recrafter.crafter.core.versions

interface VersionBound {

    override fun toString(): String

    fun isInclusive(): Boolean =
        this is InclusiveVersion

    companion object {

        fun inclusive(version: String): InclusiveVersion =
            InclusiveVersion(version)

        fun exclusive(version: String): ExclusiveVersion =
            ExclusiveVersion(version)
    }
}
