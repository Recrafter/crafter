package io.github.recrafter.crafter.versions

@JvmInline
value class ExclusiveVersion(val value: String) : VersionBound {
    override fun toString(): String = value
}
