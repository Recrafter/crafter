package io.github.recrafter.crafter.core.extensions

import io.github.recrafter.crafter.core.mixins.MixinsHelper
import io.github.recrafter.crafter.core.mixins.accessors.AccessorsHelper
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get

val SourceSetContainer.mixins: SourceSet
    get() = this[MixinsHelper.MIXINS_NAME]

val SourceSetContainer.accessors: SourceSet
    get() = this[AccessorsHelper.ACCESSORS_NAME]
