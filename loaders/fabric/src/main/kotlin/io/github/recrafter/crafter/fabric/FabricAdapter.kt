package io.github.recrafter.crafter.fabric

import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.fabric.extensions.quilt
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

object FabricAdapter : FabricFamilyAdapter(ModLoaderType.FABRIC) {

    override fun getMappingsDependency(project: Project, mod: Mod): Dependency = with(project) {
        @Suppress("UnstableApiUsage")
        quilt.layered { officialMojangMappings() }
    }
}
