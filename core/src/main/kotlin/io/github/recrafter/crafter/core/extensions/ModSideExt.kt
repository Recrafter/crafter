package io.github.recrafter.crafter.core.extensions

import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.words.PascalCase
import io.github.recrafter.bedrock.sides.ModSide

fun ModSide.getRunTaskName(): String =
    "run" + getName(PascalCase)
