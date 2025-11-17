package io.github.recrafter.crafter.core.sync.parchment

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.removePrefix
import io.github.diskria.kotlin.utils.extensions.serialization.deserializeFromJson
import io.github.diskria.kotlin.utils.extensions.toSemver
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.core.sync.common.ComponentSynchronizer
import io.github.recrafter.crafter.core.sync.common.MinecraftComponent
import io.github.recrafter.crafter.core.sync.jfrog.JFrogFolderInfo
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit

object ParchmentSynchronizer : ComponentSynchronizer() {

    override val componentName: String = "parchment-mappings"

    override val cacheDurationMillis: Long = TimeUnit.DAYS.toMillis(14)

    override suspend fun fetchComponents(): List<MinecraftComponent> = coroutineScope {
        getSubfolderNames(getParchmentFolderUrl()).map { name ->
            async {
                val minecraftVersion = MinecraftVersion.parseOrNull(name.substringAfterLast(Constants.Char.HYPHEN))
                    ?: return@async null

                val subfolders = getSubfolderNames(getParchmentFolderUrl(name))
                val latestParchmentVersion = subfolders.maxByOrNull { it.toSemver() } ?: return@async null
                MinecraftComponent(minecraftVersion, latestParchmentVersion)
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun getSubfolderNames(folderUrl: Url): List<String> =
        HttpClient(CIO).use { client ->
            val folderInfo = client.get(folderUrl).bodyAsText().deserializeFromJson<JFrogFolderInfo>()
            val subfolders = folderInfo.children.filter { it.isFolder }
            subfolders.map { it.uri.removePrefix(Constants.Char.SLASH) }
        }

    private fun getParchmentFolderUrl(extraFolders: String? = null): Url =
        buildUrl("ldtteam.jfrog.io") {
            path("artifactory", "api", "storage", "parchmentmc-internal", "org", "parchmentmc", "data")
            extraFolders?.let { appendPathSegments(it) }
        }
}
