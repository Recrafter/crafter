package io.github.recrafter.crafter.neoforge.sync

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.Semver
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.toSemverOrNull
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.core.sync.maven.MavenComponentSynchronizer
import io.github.recrafter.crafter.core.sync.maven.MavenMetadata
import io.ktor.http.*
import java.util.concurrent.TimeUnit

object NeoForgeLoaderSynchronizer : MavenComponentSynchronizer() {

    override val loader: ModLoaderType = ModLoaderType.NEOFORGE

    override val componentName: String = "loader"

    override val cacheDurationMillis: Long = TimeUnit.DAYS.toMillis(7)

    override val mavenUrl: Url =
        buildUrl("maven.neoforged.net") {
            path("releases", "net", "neoforged", "neoforge", MavenMetadata.FILE_NAME)
        }

    override fun parseMinecraftVersion(version: String): MinecraftVersion? {
        val (major, minor, _) = version.substringBefore(Constants.Char.HYPHEN).toSemverOrNull() ?: return null
        return MinecraftVersion.parseOrNull(Semver(1, major, minor).toVersion())
    }

    override fun parseComponentSemver(version: String): Semver =
        version.substringBefore(Constants.Char.HYPHEN).toSemverOrNull() ?: Semver.from(0, 0, 0)
}