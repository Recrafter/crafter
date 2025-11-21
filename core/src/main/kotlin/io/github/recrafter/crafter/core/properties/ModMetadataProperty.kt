package io.github.recrafter.crafter.core.properties

import io.github.diskria.kotlin.utils.extensions.serialization.deserializeFromJson
import io.github.diskria.kotlin.utils.extensions.serialization.serializeToJson
import io.github.recrafter.crafter.core.ModMetadata
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class ModMetadataProperty @Inject constructor(objects: ObjectFactory) {

    @get:Input
    protected val json: Property<String> = objects.property()

    fun set(mod: ModMetadata) {
        json = mod.serializeToJson()
    }

    fun get(): ModMetadata =
        json.get().deserializeFromJson()
}
