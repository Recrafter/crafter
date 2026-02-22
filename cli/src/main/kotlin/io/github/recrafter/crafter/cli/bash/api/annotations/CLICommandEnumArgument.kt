package io.github.recrafter.crafter.cli.bash.api.annotations

import io.github.recrafter.crafter.cli.bash.api.commands.arguments.enums.CLIArgumentEnum
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlin.reflect.KClass

@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class CLICommandEnumArgument(
    val description: String,
    val enumClass: KClass<out CLIArgumentEnum<*>>
)
