package io.github.recrafter.crafter.loaders.babric.sync

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.Semver
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.core.sync.common.MappingsSync
import io.github.recrafter.crafter.core.sync.maven.MavenMetadata
import io.ktor.http.*

object BarnMappingsSync : MappingsSync(ModLoaderType.BABRIC) {

    override val mavenUrl: Url =
        buildUrl("maven.glass-launcher.net") {
            path("babric", "babric", "barn", MavenMetadata.FILE_NAME)
        }

    override fun parseMinecraftVersion(version: String): MinecraftVersion? =
        MinecraftVersion.parseOrNull(version.substringBefore(Constants.Char.PLUS))

    override fun parseComponentSemver(version: String): Semver =
        Semver.from(0, 0, version.substringAfterLast(Constants.Char.DOT).toInt())
}
