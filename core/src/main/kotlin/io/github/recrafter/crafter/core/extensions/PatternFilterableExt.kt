package io.github.recrafter.crafter.core.extensions

import org.gradle.api.tasks.util.PatternFilterable

fun PatternFilterable.includeFilesWithExtension(extension: String) {
    include("**/*.$extension")
}

fun PatternFilterable.excludeFilesWithExtension(extension: String) {
    exclude("**/*.$extension")
}
