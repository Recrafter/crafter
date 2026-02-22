package io.github.recrafter.crafter.loaders.babric

import io.github.diskria.gradle.utils.extensions.common.artifact
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.loaders.babric.extensions.babric
import io.github.recrafter.crafter.loaders.fabric.FabricFamilyAdapter
import io.ktor.http.*
import org.gradle.api.Project

object BabricAdapter : FabricFamilyAdapter(ModLoaderType.BABRIC) {

    override val extensionPluginPackageName: String = "babric"

    override fun configureExtensionPlugin(project: Project) = with(project) {
        babric {}
    }

    override fun getMappingsDependency(project: Project, mod: Mod): String =
        artifact("net.glasslauncher", "biny", mod.loaderMetadata.mappingsVersion.orEmpty(), "v2")

    override fun getCustomMinecraftMetadataUrl(minecraftVersion: MinecraftVersion): Url =
        buildUrl("babric.github.io") {
            path("manifest-polyfill", fileName(minecraftVersion.asString(), Constants.File.Extension.JSON))
        }

    override fun getCustomIntermediaryUrl(placeholder: String): String =
        buildUrl("maven.glass-launcher.net") {
            path("babric", "babric", "intermediary")
        }.toString()
            .appendPath(placeholder)
            .appendPath(fileName("intermediary-$placeholder-v2", Constants.File.Extension.JAR))
}
