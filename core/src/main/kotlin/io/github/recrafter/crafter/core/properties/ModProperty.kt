package io.github.recrafter.crafter.core.properties

import io.github.diskria.kotlin.utils.extensions.serialization.deserializeFromJson
import io.github.diskria.kotlin.utils.extensions.serialization.serializeToJson
import io.github.recrafter.crafter.core.Mod
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class ModProperty @Inject constructor(objects: ObjectFactory) {

    @get:Input
    protected val json: Property<String> = objects.property()

    fun set(mod: Mod) {
        json = mod.serializeToJson()
    }

    fun get(): Mod =
        json.get().deserializeFromJson()
}
