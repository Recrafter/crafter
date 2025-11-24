package io.github.recrafter.crafter.cli.bash.builder

@JvmInline
value class Lambda(val command: String) {
    override fun toString(): String = command
}
