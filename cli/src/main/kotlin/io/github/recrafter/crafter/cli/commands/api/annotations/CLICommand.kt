package io.github.recrafter.crafter.cli.commands.api.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CLICommand(val name: String, val description: String)
