package io.github.recrafter.crafter.cli.commands.api.annotations

import io.github.diskria.kotlin.utils.extensions.common.KotlinClass
import io.github.recrafter.crafter.cli.commands.api.common.CLIArgumentEnum
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class CLICommandEnumArgument(
    val description: String,
    val enumClass: KotlinClass<out CLIArgumentEnum<*>>
)
