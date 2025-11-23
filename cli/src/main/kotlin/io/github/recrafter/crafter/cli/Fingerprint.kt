package io.github.recrafter.crafter.cli

data class Fingerprint(
    val pluginVersion: String,
    val gradleTaskName: String,
    val scriptName: String,
    val completionScriptName: String,
    val modEnvironment: String,
    val modNamespace: String,
    val modId: String,
    val loaderVersions: Map<String, String>,
)