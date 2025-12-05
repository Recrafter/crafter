package io.github.recrafter.crafter.loaders.quilt

import io.github.diskria.gradle.utils.extensions.common.artifact
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.loaders.fabric.FabricFamilyAdapter

object QuiltAdapter : FabricFamilyAdapter(ModLoaderType.QUILT) {

    override fun getLoaderDependency(mod: Mod): String =
        artifact("org.quiltmc", "quilt-loader", mod.loaderMetadata.loaderVersion)
}
