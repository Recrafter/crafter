package io.github.recrafter.crafter.cli.extensions

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementNames

@OptIn(ExperimentalSerializationApi::class)
val SerialDescriptor.elementAnnotations: Map<String, List<Annotation>>
    get() = elementNames
        .mapIndexed { index, name -> name to getElementAnnotations(index) }
        .toMap()
