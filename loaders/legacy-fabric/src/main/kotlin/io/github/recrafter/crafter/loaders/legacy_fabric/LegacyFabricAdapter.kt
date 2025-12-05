package io.github.recrafter.crafter.loaders.legacy_fabric

import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.loaders.fabric.FabricFamilyAdapter
import io.github.recrafter.crafter.loaders.legacy_fabric.extensions.legacyFabric
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

object LegacyFabricAdapter : FabricFamilyAdapter(ModLoaderType.LEGACY_FABRIC) {

    override val extensionPluginPackageName: String = "net.legacyfabric.legacylooming"

    override fun configureExtensionPlugin(project: Project) = with(project) {
        legacyFabric {}
    }

    override fun getMappingsDependency(project: Project, mod: Mod): Dependency = with(project) {
        legacyFabric.yarn(mod.loaderMetadata.minecraftVersion.asString(), mod.loaderMetadata.mappingsVersion.orEmpty())
    }
}
