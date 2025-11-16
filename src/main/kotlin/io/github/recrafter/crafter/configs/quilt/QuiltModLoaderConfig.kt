package io.github.recrafter.crafter.configs.quilt

import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.models.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuiltModLoaderConfig(
    @SerialName("group")
    val namespace: String,
    val id: String,
    val version: String,
    val metadata: QuiltModMetadataConfig,

    @SerialName("intermediate_mappings")
    val mappings: String,

    @SerialName("entrypoints")
    val entryPoints: QuiltModEntryPointsConfig,

    @SerialName("depends")
    val dependencies: List<QuiltModDependencyConfig>,
) {
    companion object {
        fun of(mod: Mod, splitSide: ModSide?): QuiltModLoaderConfig =
            QuiltModLoaderConfig(
                namespace = mod.namespace,
                id = mod.id,
                version = mod.version,
                metadata = QuiltModMetadataConfig.of(mod),
                mappings = "net.fabricmc:intermediary",
                entryPoints = QuiltModEntryPointsConfig.of(mod, splitSide),
                dependencies = listOf(
                    QuiltModDependencyConfig.createJavaDependency(mod),
                    QuiltModDependencyConfig.createMinecraftDependency(mod),
                    QuiltModDependencyConfig.createLoaderDependency(mod),
                ),
            )
    }
}
