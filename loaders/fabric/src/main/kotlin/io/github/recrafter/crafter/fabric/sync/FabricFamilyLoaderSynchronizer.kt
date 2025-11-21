package io.github.recrafter.crafter.fabric.sync

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.Semver
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.common.failWithUnsupportedType
import io.github.diskria.kotlin.utils.extensions.toSemverOrNull
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.core.extensions.supportedVersionRange
import io.github.recrafter.crafter.core.sync.maven.MavenComponentSynchronizer
import io.github.recrafter.crafter.core.sync.maven.MavenMetadata
import io.ktor.http.*

class FabricFamilyLoaderSynchronizer(override val loader: ModLoaderType) : MavenComponentSynchronizer() {

    override val componentName: String = "loader"

    override val mavenUrl: Url
        get() = when (loader) {
            ModLoaderType.FABRIC, ModLoaderType.LEGACY_FABRIC, ModLoaderType.ORNITHE -> {
                buildUrl("maven.fabricmc.net") {
                    path("net", "fabricmc", "fabric-loader", MavenMetadata.FILE_NAME)
                }
            }

            ModLoaderType.QUILT -> {
                buildUrl("maven.quiltmc.org") {
                    path("repository", "release", "org", "quiltmc", "quilt-loader", MavenMetadata.FILE_NAME)
                }
            }

            ModLoaderType.BABRIC -> {
                buildUrl("maven.glass-launcher.net") {
                    path("babric", "babric", "fabric-loader", MavenMetadata.FILE_NAME)
                }
            }

            else -> failWithUnsupportedType(loader::class)
        }

    override fun parseMinecraftVersion(version: String): MinecraftVersion =
        loader.supportedVersionRange.min

    override fun parseComponentSemver(version: String): Semver =
        when (loader) {
            ModLoaderType.FABRIC, ModLoaderType.LEGACY_FABRIC, ModLoaderType.ORNITHE -> {
                version.toSemverOrNull()
            }

            ModLoaderType.QUILT -> {
                version.substringBefore(Constants.Char.HYPHEN).toSemverOrNull()
            }

            ModLoaderType.BABRIC -> {
                val semver = version.substringBefore(Constants.Char.HYPHEN).toSemverOrNull()
                semver?.copy(
                    patch = semver.patch * 10 + version.substringAfterLast(Constants.Char.DOT).toInt()
                )
            }

            else -> null
        } ?: Semver.from(0, 0, 0)
}
