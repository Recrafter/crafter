package io.github.recrafter.crafter.neoforge.sync

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.Semver
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.toSemverOrNull
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.core.sync.common.LoaderSync
import io.github.recrafter.crafter.core.sync.maven.MavenMetadata
import io.ktor.http.*

object NeoForgeLoaderSync : LoaderSync(ModLoaderType.NEOFORGE) {

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