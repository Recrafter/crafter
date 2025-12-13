package io.github.recrafter.crafter.core.versions

interface VersionBound {

    fun isInclusive(): Boolean =
        this is InclusiveVersion

    fun isExclusive(): Boolean =
        this is ExclusiveVersion

    override fun toString(): String

    companion object {

        fun inclusive(version: String): InclusiveVersion =
            InclusiveVersion(version)

        fun inclusive(version: Int): InclusiveVersion =
            inclusive(version.toString())

        fun exclusive(version: String): ExclusiveVersion =
            ExclusiveVersion(version)

        fun exclusive(version: Int): ExclusiveVersion =
            exclusive(version.toString())
    }
}
