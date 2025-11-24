package io.github.recrafter.crafter.core.sync.packs.common

import io.github.diskria.gradle.utils.extensions.common.requireGradleNotNull
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.Semver
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.core.sync.common.ComponentSync
import io.github.recrafter.crafter.core.sync.common.MinecraftComponent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jsoup.Jsoup

abstract class PackFormatSync : ComponentSync() {

    protected abstract val wikiTableCaption: String

    override suspend fun fetchComponents(): List<MinecraftComponent> =
        HttpClient(CIO).use { client ->
            val wikiUrl = buildUrl("minecraft.wiki") {
                path("w", "Pack_format")
            }
            val wikiHtml = client.get(wikiUrl).bodyAsText()
            val formatComponents = Jsoup.parse(wikiHtml)
                .select("caption")
                .firstOrNull { it.ownText().trim() == wikiTableCaption }
                ?.parent()
                ?.select("tr#pack-format-column")
                ?.mapNotNull { tableRow ->
                    val format = tableRow.selectFirst("th#pack-format")?.text()?.trim() ?: return@mapNotNull null
                    val versionRange = tableRow.selectFirst("th#v")?.text()?.trim() ?: return@mapNotNull null
                    val minecraftVersion = parseMinecraftVersion(versionRange) ?: return@mapNotNull null
                    MinecraftComponent(minecraftVersion, format)
                }
            requireGradleNotNull(formatComponents) {
                "Failed to parse formats."
            }
        }

    private fun parseMinecraftVersion(version: String): MinecraftVersion? =
        MinecraftVersion.parseOrNull(version.substringBefore(Constants.Char.EN_DASH).trim())

    override fun parseComponentSemver(version: String): Semver =
        Semver.parse(version)
}
