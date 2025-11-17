package io.github.recrafter.crafter.core.versions

@JvmInline
value class ExclusiveVersion(val value: String) : VersionBound {
    override fun toString(): String = value
}
