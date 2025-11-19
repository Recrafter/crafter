package io.github.recrafter.crafter.core.extensions

import io.github.diskria.gradle.utils.helpers.jvm.JavaConstants
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

fun JvmTarget.toInt(): Int =
    if (this == JvmTarget.JVM_1_8) JavaConstants.VERSION_8
    else target.toInt()

fun Int.toJvmTarget(): JvmTarget =
    if (this == JavaConstants.VERSION_8) JvmTarget.JVM_1_8
    else JvmTarget.fromTarget(toString())
