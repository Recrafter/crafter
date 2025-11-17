package io.github.recrafter.crafter.ornithe.sync

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.Semver
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MappingsType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.core.sync.maven.MavenComponentSynchronizer
import io.github.recrafter.crafter.core.sync.maven.MavenMetadata
import io.ktor.http.Url
import io.ktor.http.path
import java.util.concurrent.TimeUnit

class OrnitheMappingsSynchronizer(
    override val mappingsType: MappingsType
) : MavenComponentSynchronizer() {

    override val loader: ModLoaderType = ModLoaderType.ORNITHE

    override val componentName: String = "feather-${mappingsType.getName()}-mappings"

    override val cacheDurationMillis: Long = TimeUnit.DAYS.toMillis(7)

    override val mavenUrl: Url =
        buildUrl("maven.ornithemc.net") {
            path("releases", "net", "ornithemc", "feather", MavenMetadata.FILE_NAME)
        }

    override fun parseMinecraftVersion(version: String): MinecraftVersion? =
        if (mappingsType == MappingsType.MERGED) {
            MinecraftVersion.parseOrNull(version.substringBefore(Constants.Char.PLUS))
        } else {
            val side = if (mappingsType == MappingsType.SPLIT) ModSide.SERVER else ModSide.CLIENT
            if (version.contains(side.getName())) {
                MinecraftVersion.parseOrNull(version.substringBefore(Constants.Char.HYPHEN + side.getName()))
            } else {
                null
            }
        }

    override fun mapLatestVersion(version: String): String =
        getBuildNumber(version).toString()

    override fun parseComponentSemver(version: String): Semver =
        Semver.from(0, 0, getBuildNumber(version))

    private fun getBuildNumber(version: String): Int =
        version.substringAfterLast(Constants.Char.DOT).toInt()
}