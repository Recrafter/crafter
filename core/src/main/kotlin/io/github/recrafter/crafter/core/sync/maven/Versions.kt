package io.github.recrafter.crafter.core.sync.maven

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
data class Versions(
    @XmlElement(true)
    val version: List<String>,
)
