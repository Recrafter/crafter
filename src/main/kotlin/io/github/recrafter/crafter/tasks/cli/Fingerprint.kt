package io.github.recrafter.crafter.tasks.cli

import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.MinecraftVersion

data class Fingerprint(
    val pluginVersion: String,
    val gradleTaskName: String,
    val scriptFileName: String,
    val completionScriptFileName: String,
    val modEnvironment: String,
    val modNamespace: String,
    val modId: String,
    val loaderVersions: Map<ModLoaderType, List<MinecraftVersion>>,
)
