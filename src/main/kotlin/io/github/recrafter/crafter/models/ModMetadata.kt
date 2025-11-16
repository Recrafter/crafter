package io.github.recrafter.crafter.models

import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModEnvironment
import io.github.recrafter.bedrock.versions.MinecraftVersion

data class ModMetadata(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val licenseId: String,
    val archiveVersion: (ModLoaderType, MinecraftVersion, MinecraftVersion) -> String,
    val homepageUrl: String?,
    val runDirectoryName: String,
    val javaVersion: Int,

    val developer: String,
    val player: String,
    val namespace: String,
    val repoUrl: String?,
    val issuesUrl: String?,
    val environment: ModEnvironment,
) {
    fun toMod(
        loader: ModLoaderType,
        minMinecraftVersion: MinecraftVersion,
        maxMinecraftVersion: MinecraftVersion,
        versionsMetadata: VersionsMetadata,
    ): Mod =
        Mod(
            id = id,
            name = name,
            description = description,
            version = version,
            licenseId = licenseId,
            archiveVersion = archiveVersion,
            homepageUrl = homepageUrl,
            runDirectoryName = runDirectoryName,
            javaVersion = javaVersion,
            developer = developer,
            player = player,
            namespace = namespace,
            repoUrl = repoUrl,
            issuesUrl = issuesUrl,
            environment = environment,
            loader = loader,
            versions = versionsMetadata,
            minMinecraftVersion = minMinecraftVersion,
            maxMinecraftVersion = maxMinecraftVersion,
        )
}
