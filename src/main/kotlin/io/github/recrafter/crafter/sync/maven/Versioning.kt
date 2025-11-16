package io.github.recrafter.crafter.sync.maven

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
data class Versioning(
    @XmlElement(true)
    val latest: String,

    @XmlElement(true)
    val release: String,

    @XmlElement(true)
    @XmlSerialName("versions")
    val versions: Versions,

    @XmlElement(true)
    val lastUpdated: String,
)
