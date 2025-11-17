package io.github.recrafter.crafter.core.extensions

import io.github.recrafter.crafter.core.helpers.MixinsHelper
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get

val SourceSetContainer.mixins: SourceSet
    get() = this[MixinsHelper.MIXINS_NAME]
