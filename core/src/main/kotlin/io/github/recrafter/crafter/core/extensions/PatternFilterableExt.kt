package io.github.recrafter.crafter.core.extensions

import org.gradle.api.tasks.util.PatternFilterable

fun PatternFilterable.includeFilesWithExtension(extension: String) {
    include("**/*.$extension")
}
