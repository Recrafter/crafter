package io.github.recrafter.crafter.core.versions

@JvmInline
value class InclusiveVersion(val value: String) : VersionBound {
    override fun toString(): String = value
}
