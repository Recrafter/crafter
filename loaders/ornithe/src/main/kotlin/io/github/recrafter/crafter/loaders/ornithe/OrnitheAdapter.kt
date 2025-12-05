package io.github.recrafter.crafter.loaders.ornithe

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.loaders.fabric.FabricFamilyAdapter
import io.github.recrafter.crafter.loaders.ornithe.extensions.ornithe
import io.ktor.http.*
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

object OrnitheAdapter : FabricFamilyAdapter(ModLoaderType.ORNITHE) {

    override val extensionPluginPackageName: String = "net.ornithemc.ploceus"

    override val customVersionManifest: Url =
        buildUrl("ornithemc.net") {
            path("mc-versions", fileName("version_manifest", Constants.File.Extension.JSON))
        }

    override fun configureExtensionPlugin(project: Project) = with(project) {
        ornithe {
            setGeneration(2)
        }
    }

    override fun getMappingsDependency(project: Project, mod: Mod): Dependency = with(project) {
        ornithe.featherMappings(mod.loaderMetadata.mappingsVersion.orEmpty())
    }
}
