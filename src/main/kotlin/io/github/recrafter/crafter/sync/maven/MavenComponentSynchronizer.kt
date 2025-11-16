package io.github.recrafter.crafter.sync.maven

import io.github.diskria.kotlin.utils.extensions.serialization.deserializeFromXml
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.sync.common.ComponentSynchronizer
import io.github.recrafter.crafter.sync.common.MinecraftComponent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

abstract class MavenComponentSynchronizer : ComponentSynchronizer() {

    abstract val mavenUrl: Url

    abstract fun parseMinecraftVersion(version: String): MinecraftVersion?

    final override suspend fun fetchComponents(): List<MinecraftComponent> =
        HttpClient(CIO).use { client ->
            val mavenMetadata = client.get(mavenUrl).bodyAsText().deserializeFromXml<MavenMetadata>()
            val versions = mavenMetadata.versioning.versions.version
            versions.mapNotNull { version ->
                val minecraftVersion = parseMinecraftVersion(version) ?: return@mapNotNull null
                MinecraftComponent(minecraftVersion, version)
            }
        }
}
