package io.github.recrafter.crafter.quilt

import io.github.diskria.gradle.utils.extensions.common.buildArtifactCoordinates
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.fabric.FabricFamilyAdapter
import io.github.recrafter.crafter.fabric.extensions.quilt
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

object QuiltAdapter : FabricFamilyAdapter(ModLoaderType.QUILT) {

    override fun getLoaderDependency(mod: Mod): String =
        buildArtifactCoordinates("org.quiltmc", "quilt-loader", mod.loaderMetadata.loaderVersion)

    override fun getMappingsDependency(project: Project, mod: Mod): Dependency = with(project) {
        @Suppress("UnstableApiUsage")
        quilt.layered { officialMojangMappings() }
    }
}
