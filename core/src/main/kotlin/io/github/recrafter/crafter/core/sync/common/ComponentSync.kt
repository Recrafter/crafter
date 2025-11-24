package io.github.recrafter.crafter.core.sync.common

import io.github.diskria.gradle.utils.extensions.common.requireGradleNotNull
import io.github.diskria.gradle.utils.extensions.rootDirectory
import io.github.diskria.gradle.utils.helpers.GradleDirectories
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.Semver
import io.github.diskria.kotlin.utils.extensions.asFileOrNull
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.common.nowMillis
import io.github.diskria.kotlin.utils.extensions.ensureFileExists
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.serialization.deserializeJsonFromFile
import io.github.diskria.kotlin.utils.extensions.serialization.serializeJsonToFile
import io.github.diskria.kotlin.utils.extensions.toSemver
import io.github.recrafter.bedrock.crafter.CrafterConstants
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.bedrock.versions.compareTo
import io.github.recrafter.bedrock.versions.contains
import io.github.recrafter.crafter.core.extensions.supportedVersionRange
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import java.io.File
import java.util.concurrent.TimeUnit

abstract class ComponentSync {

    protected open val loader: ModLoaderType? = null

    protected abstract val componentName: String

    protected open val cacheDurationMillis: Long = TimeUnit.DAYS.toMillis(1)

    protected open fun mapLatestVersion(version: String): String = version

    protected open fun parseComponentSemver(version: String): Semver = version.toSemver()

    protected abstract suspend fun fetchComponents(): List<MinecraftComponent>

    open fun getLatestVersion(project: Project, minecraftVersion: MinecraftVersion): String =
        getLatestComponent(project, minecraftVersion).latestVersion

    fun getMinecraftVersion(project: Project, minecraftVersion: MinecraftVersion): MinecraftVersion =
        getLatestComponent(project, minecraftVersion).minecraftVersion

    private fun getLatestComponent(project: Project, minecraftVersion: MinecraftVersion): MinecraftComponent {
        val cacheFile = getCacheFile(project)
        val cache = cacheFile.asFileOrNull()?.deserializeJsonFromFile<MinecraftComponents>()
        val components = cache?.takeIf { nowMillis() - it.lastSyncMillis < cacheDurationMillis } ?: runBlocking {
            val versions = fetchComponents()
                .groupBy { it.minecraftVersion }
                .filterKeys { minecraftVersion -> loader?.supportedVersionRange?.contains(minecraftVersion) ?: true }
                .mapValues {
                    val version = it.value.maxBy { version -> parseComponentSemver(version.latestVersion) }
                    version.copy(latestVersion = mapLatestVersion(version.latestVersion))
                }
                .values
                .sortedWith(compareBy(MinecraftVersion.COMPARATOR) { it.minecraftVersion })
            MinecraftComponents(versions, nowMillis()).also { it.serializeJsonToFile(cacheFile.ensureFileExists()) }
        }
        val latestComponent = components.versions
            .filter { it.minecraftVersion <= minecraftVersion }
            .maxWithOrNull(compareBy(MinecraftVersion.COMPARATOR) { it.minecraftVersion })
        return requireGradleNotNull(latestComponent) {
            "Latest component for Minecraft ${minecraftVersion.asString()}" +
                    "not found in cache file: ${cacheFile.relativeTo(project.rootDirectory)}"
        }
    }

    private fun getCacheFile(project: Project): File {
        val cacheRoot = project
            .rootDirectory
            .resolve(GradleDirectories.CACHE)
            .resolve(CrafterConstants.PLUGIN_LOWER_NAME)
        val parentDirectory = loader?.let { cacheRoot.resolve(it.getName(`kebab-case`)) } ?: cacheRoot
        return parentDirectory.resolve(fileName("$componentName-versions", Constants.File.Extension.JSON))
    }
}
