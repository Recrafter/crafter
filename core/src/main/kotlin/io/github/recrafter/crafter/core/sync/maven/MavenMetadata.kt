package io.github.recrafter.crafter.core.sync.maven

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.diskria.kotlin.utils.serialization.annotations.EncodeDefaults
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@EncodeDefaults
data class MavenMetadata(
    @XmlElement(true)
    val groupId: String,

    @XmlElement(true)
    val artifactId: String,

    @XmlElement(true)
    @XmlSerialName("versioning")
    val versioning: Versioning,
) {
    companion object {
        val FILE_NAME: String = fileName("maven-metadata", Constants.File.Extension.XML)
    }
}
