package io.github.recrafter.crafter.sync.loaders.babric

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.Semver
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.toSemverOrNull
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.sync.maven.MavenComponentSynchronizer
import io.github.recrafter.crafter.sync.maven.MavenMetadata
import io.ktor.http.*
import java.util.concurrent.TimeUnit

object BabricLoaderSynchronizer : MavenComponentSynchronizer() {

    override val loader: ModLoaderType = ModLoaderType.BABRIC

    override val componentName: String = "loader"

    override val cacheDurationMillis: Long = TimeUnit.DAYS.toMillis(7)

    override val mavenUrl: Url =
        buildUrl("maven.glass-launcher.net") {
            path("babric", "babric", "fabric-loader", MavenMetadata.FILE_NAME)
        }

    override fun parseMinecraftVersion(version: String): MinecraftVersion =
        MinecraftVersion.EARLIEST

    override fun parseComponentSemver(version: String): Semver {
        val semver = version.substringBefore(Constants.Char.HYPHEN).toSemverOrNull() ?: Semver.from(0, 0, 0)
        return semver.copy(
            patch = semver.patch * 10 + version.substringAfterLast(Constants.Char.DOT).toInt()
        )
    }
}
