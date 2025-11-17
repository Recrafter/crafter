package io.github.recrafter.crafter.core

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.gradle.utils.helpers.JarConstants
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPackageName
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.*
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.words.PascalCase
import io.github.recrafter.bedrock.MinecraftConstants
import io.github.recrafter.bedrock.era.Release
import io.github.recrafter.bedrock.loaders.ModLoaderFamily
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModEnvironment
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.*
import java.util.*

data class Mod(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val licenseId: String,
    val developer: String,
    val namespace: String,
    val javaVersion: Int,
    val player: String,
    val environment: ModEnvironment,
    val repoUrl: String?,
    val issuesUrl: String?,
    val homepageUrl: String?,
    val runDirectoryName: String,
    val archiveVersion: (ModLoaderType, MinecraftVersion, MinecraftVersion) -> String,
    val loader: ModLoaderType,
    val versions: VersionsMetadata,
    val minMinecraftVersion: MinecraftVersion,
    val maxMinecraftVersion: MinecraftVersion,
) {
    val packageName: String = namespace.appendPackageName(id.setCase(snake_case, `dot․case`))
    val packagePath: String = packageName.setCase(`dot․case`, `path∕case`)
    val minecraftVersion: MinecraftVersion = minMinecraftVersion

    val loaderFamily: ModLoaderFamily =
        ModLoaderFamily.of(loader)

    val assetsPath: String =
        "assets".appendPath(id)

    val iconFileName: String =
        fileName("icon", Constants.File.Extension.PNG)

    val iconPath: String =
        assetsPath.appendPath(iconFileName)

    val accessConfigName: String =
        when (loaderFamily) {
            ModLoaderFamily.FABRIC -> fileName(id, "accesswidener")
            ModLoaderFamily.FORGE -> fileName("accesstransformer", "cfg")
        }

    val accessConfigPath: String
        get() {
            val parentDirectory = when (loader) {
                ModLoaderType.FORGE -> JarConstants.Directory.META_INF
                else -> assetsPath
            }
            return parentDirectory.appendPath(accessConfigName)
        }

    val mixinsConfigName: String =
        fileName(id, "mixins", Constants.File.Extension.JSON)

    val mixinsConfigPath: String =
        assetsPath.appendPath(mixinsConfigName)

    val resourcePackConfigName: String =
        fileName("pack", "mcmeta")

    val refmapFileName: String =
        fileName(id + "_refmap", Constants.File.Extension.JSON)

    val configName: String =
        when (loaderFamily) {
            ModLoaderFamily.FABRIC -> {
                fileName(
                    if (loader == ModLoaderType.QUILT) loader.getName() else ModLoaderFamily.FABRIC.getName(),
                    "mod",
                    Constants.File.Extension.JSON
                )
            }

            ModLoaderFamily.FORGE -> when {
                loader == ModLoaderType.NEOFORGE && minecraftVersion >= Release.V_1_20_5 -> {
                    fileName(ModLoaderType.NEOFORGE.getName(), "mods", Constants.File.Extension.TOML)
                }

                else -> fileName("mods", Constants.File.Extension.TOML)
            }
        }

    val configParentPath: String =
        when (loaderFamily) {
            ModLoaderFamily.FABRIC -> Constants.Char.EMPTY
            ModLoaderFamily.FORGE -> JarConstants.Directory.META_INF
        }

    val configEnvironment: String
        get() {
            val splitSide = if (environment == ModEnvironment.SERVER_ONLY) null else environment.sides.singleOrNull()
            return when (loaderFamily) {
                ModLoaderFamily.FABRIC -> splitSide?.getName() ?: Constants.Char.ASTERISK.toString()
                ModLoaderFamily.FORGE -> splitSide?.getName(SCREAMING_SNAKE_CASE) ?: "BOTH"
            }
        }

    val offlinePlayerUUID: UUID =
        UUID.nameUUIDFromBytes("OfflinePlayer:$player".toByteArray(Charsets.UTF_8))

    val minJavaVersion: Int
        get() {
            val minJava = minMinecraftVersion.minJavaVersion
            val maxJava = maxMinecraftVersion.minJavaVersion
            if (minJava != maxJava) {
                gradleError("Minecraft version range crosses Java compatibility: $minJava -> $maxJava")
            }
            return maxJava
        }

    fun getEntryPointName(side: ModSide): String =
        buildString {
            append(MinecraftConstants.FULL_GAME_NAME)
            if (minecraftVersion.mappingsType != MappingsType.MERGED ||
                side == ModSide.CLIENT ||
                environment == ModEnvironment.DEDICATED_SERVER_ONLY
            ) {
                append(side.getName(PascalCase))
            }
            append("Mod")
        }
}
