package io.github.recrafter.crafter.cli.extensions

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.className
import kotlin.reflect.KClass

val KClass<out Annotation>.atName: String
    get() = Constants.Char.AT_SIGN + className()
