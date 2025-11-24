package io.github.recrafter.crafter.forge.sync

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.Semver
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.toSemver
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.core.sync.common.LoaderSync
import io.github.recrafter.crafter.core.sync.maven.MavenMetadata
import io.ktor.http.*

object ForgeLoaderSync : LoaderSync(ModLoaderType.FORGE) {

    override val mavenUrl: Url =
        buildUrl("maven.minecraftforge.net") {
            path("net", "minecraftforge", "forge", MavenMetadata.FILE_NAME)
        }

    override fun mapLatestVersion(version: String): String =
        version.substringAfterLast(Constants.Char.HYPHEN)

    override fun parseMinecraftVersion(version: String): MinecraftVersion? =
        MinecraftVersion.parseOrNull(version.substringBefore(Constants.Char.HYPHEN))

    override fun parseComponentSemver(version: String): Semver =
        version.substringAfterLast(Constants.Char.HYPHEN).toSemver()
}
