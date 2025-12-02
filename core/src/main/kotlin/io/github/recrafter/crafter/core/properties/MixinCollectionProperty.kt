package io.github.recrafter.crafter.core.properties

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.io.File

@JvmInline
value class MixinCollectionProperty(
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val collection: ConfigurableFileCollection
) {
    val files: Set<File> get() = collection.files
}
