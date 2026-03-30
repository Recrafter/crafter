package io.github.recrafter.crafter.loaders.neoforge.sync

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
        val numericPart = version.substringBefore(Constants.Char.HYPHEN)
        val segments = numericPart.split(Constants.Char.DOT)
        return MinecraftVersion.parseOrNull(
            if (segments.size >= 4) {
                val major = segments[0]
                val minor = segments[1]
                val patch = segments[2]
                if (patch == "0") "$major.$minor" else "$major.$minor.$patch"
            } else {
                val oldFormatSemver = numericPart.toSemverOrNull() ?: return null
                "1.${oldFormatSemver.major}.${oldFormatSemver.minor}"
            }
        )
    }

    override fun parseComponentSemver(version: String): Semver {
        val numericPart = version.substringBefore(Constants.Char.HYPHEN)
        val segments = numericPart.split(Constants.Char.DOT)
        return if (segments.size >= 4) {
            val major = "${segments[0]}${segments[1]}".toIntOrNull() ?: 0
            val minor = segments[2].toIntOrNull() ?: 0
            val patch = segments[3].toIntOrNull() ?: 0
            Semver(major, minor, patch)
        } else {
            numericPart.toSemverOrNull() ?: Semver.from(0, 0, 0)
        }
    }
}
