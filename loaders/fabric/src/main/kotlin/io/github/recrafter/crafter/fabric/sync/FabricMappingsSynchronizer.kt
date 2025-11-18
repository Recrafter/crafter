package io.github.recrafter.crafter.fabric.sync

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.Semver
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.core.sync.maven.MavenComponentSynchronizer
import io.github.recrafter.crafter.core.sync.maven.MavenMetadata
import io.ktor.http.*
import java.util.concurrent.TimeUnit

object FabricMappingsSynchronizer : MavenComponentSynchronizer() {

    override val loader: ModLoaderType = ModLoaderType.FABRIC

    override val componentName: String = "mappings"

    override val cacheDurationMillis: Long = TimeUnit.DAYS.toMillis(7)

    override val mavenUrl: Url =
        buildUrl("maven.fabricmc.net") {
            path("net", "fabricmc", "yarn", MavenMetadata.FILE_NAME)
        }

    override fun parseMinecraftVersion(version: String): MinecraftVersion? =
        MinecraftVersion.parseOrNull(version.substringBefore(Constants.Char.PLUS))

    override fun parseComponentSemver(version: String): Semver =
        Semver.from(0, 0, version.substringAfterLast(Constants.Char.DOT).toInt())
}
