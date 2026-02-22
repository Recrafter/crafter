package io.github.recrafter.crafter.loaders.babric.sync

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.core.sync.common.MappingsSync
import io.github.recrafter.crafter.core.sync.maven.MavenMetadata
import io.ktor.http.*

object BinyMappingsSync : MappingsSync(ModLoaderType.BABRIC) {

    override val useLatestRelease: Boolean = true

    override val mavenUrl: Url =
        buildUrl("maven.glass-launcher.net") {
            path("releases", "net", "glasslauncher", "biny", MavenMetadata.FILE_NAME)
        }

    override fun parseMinecraftVersion(version: String): MinecraftVersion? =
        MinecraftVersion.parseOrNull(version.substringBefore(Constants.Char.PLUS))
}
