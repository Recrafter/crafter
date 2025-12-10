package io.github.recrafter.crafter.cli

import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MinecraftVersion

data class Fingerprint(
    val pluginVersion: String,
    val gradleTaskName: String,
    val scriptName: String,
    val completionScriptName: String,
    val loaders: List<LoaderInfo>,
    val modId: String,
    val modNamespace: String,
    val modSides: List<ModSide>,
) {
    data class LoaderInfo(
        val name: String,
        val displayName: String,
        val supportedVersions: List<MinecraftVersion>,
        val checkpoints: List<MinecraftVersion>,
    )
}
