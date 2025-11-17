package io.github.recrafter.crafter.fabric.sync

import io.github.diskria.kotlin.utils.Semver
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.toSemverOrNull
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.MappingsType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.core.sync.maven.MavenComponentSynchronizer
import io.github.recrafter.crafter.core.sync.maven.MavenMetadata
import io.ktor.http.*
import java.util.concurrent.TimeUnit

object FabricLoaderSynchronizer : MavenComponentSynchronizer() {

    override val loader: ModLoaderType = ModLoaderType.ORNITHE

    override val mappingsType: MappingsType = MappingsType.CLIENT

    override val componentName: String = "loader"

    override val cacheDurationMillis: Long = TimeUnit.DAYS.toMillis(7)

    override val mavenUrl: Url =
        buildUrl("maven.fabricmc.net") {
            path("net", "fabricmc", "fabric-loader", MavenMetadata.FILE_NAME)
        }

    override fun parseMinecraftVersion(version: String): MinecraftVersion =
        MinecraftVersion.EARLIEST

    override fun parseComponentSemver(version: String): Semver =
        version.toSemverOrNull() ?: Semver.from(0, 0, 0)
}
