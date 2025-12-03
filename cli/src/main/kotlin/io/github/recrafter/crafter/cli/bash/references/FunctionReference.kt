package io.github.recrafter.crafter.cli.bash.references

@JvmInline
value class FunctionReference(val name: String) {
    override fun toString(): String = name
}
