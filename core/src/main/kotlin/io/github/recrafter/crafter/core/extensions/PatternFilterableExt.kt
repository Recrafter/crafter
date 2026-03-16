package io.github.recrafter.crafter.core.extensions

import org.gradle.api.tasks.util.PatternFilterable

fun PatternFilterable.excludeFilesWithExtension(extension: String) {
    exclude("**/*.$extension")
}

fun PatternFilterable.excludeDirectory(vararg paths: String) {
    paths.forEach { exclude("$it/**") }
}
