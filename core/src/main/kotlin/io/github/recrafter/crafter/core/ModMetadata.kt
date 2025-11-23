package io.github.recrafter.crafter.core

import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModEnvironment
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.isInternalServer
import kotlinx.serialization.Serializable

@Serializable
data class ModMetadata(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val licenseId: String,
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
        loaderMetadata: LoaderMetadata,
    ): Mod =
        Mod(
            id = id,
            name = name,
            description = description,
            version = version,
            licenseId = licenseId,
            homepageUrl = homepageUrl,
            runDirectoryName = runDirectoryName,
            javaVersion = javaVersion,
            developer = developer,
            player = player,
            namespace = namespace,
            repoUrl = repoUrl,
            issuesUrl = issuesUrl,
            environment = if (minMinecraftVersion.isInternalServer) ModEnvironment.CLIENT_ONLY else environment,
            loader = loader,
            loaderMetadata = loaderMetadata,
            minMinecraftVersion = minMinecraftVersion,
            maxMinecraftVersion = maxMinecraftVersion,
        )
}
