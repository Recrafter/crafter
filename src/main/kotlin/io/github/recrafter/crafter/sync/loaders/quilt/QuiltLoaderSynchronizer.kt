package io.github.recrafter.crafter.sync.loaders.quilt

import io.github.diskria.kotlin.utils.Semver
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.toSemverOrNull
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.MappingsType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.sync.maven.MavenComponentSynchronizer
import io.github.recrafter.crafter.sync.maven.MavenMetadata
import io.ktor.http.*
import java.util.concurrent.TimeUnit

object QuiltLoaderSynchronizer : MavenComponentSynchronizer() {

    override val loader: ModLoaderType = ModLoaderType.QUILT

    override val componentName: String = "loader"

    override val cacheDurationMillis: Long = TimeUnit.DAYS.toMillis(7)

    override val mavenUrl: Url =
        buildUrl("maven.quiltmc.org") {
            path("repository", "release", "org", "quiltmc", "quilt-loader", MavenMetadata.FILE_NAME)
        }

    override fun parseMinecraftVersion(version: String): MinecraftVersion =
        MinecraftVersion.EARLIEST

    override fun parseComponentSemver(version: String): Semver =
        version.toSemverOrNull() ?: Semver.from(0, 0, 0)
}
