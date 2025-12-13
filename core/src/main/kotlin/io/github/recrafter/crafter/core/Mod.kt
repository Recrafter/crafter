package io.github.recrafter.crafter.core

import io.github.diskria.gradle.utils.extensions.log
import io.github.diskria.gradle.utils.helpers.JarConstants
import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPackageName
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.`dot․case`
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.diskria.kotlin.utils.extensions.common.`path∕case`
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.setCase
import io.github.diskria.kotlin.utils.extensions.wrapWithBrackets
import io.github.diskria.kotlin.utils.words.PascalCase
import io.github.recrafter.bedrock.MinecraftConstants
import io.github.recrafter.bedrock.crafter.CrafterConstants
import io.github.recrafter.bedrock.loaders.ModLoaderFamily
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModEnvironment
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.*
import io.github.recrafter.crafter.core.extensions.family
import io.github.recrafter.crafter.core.extensions.toJvmTarget
import kotlinx.serialization.Serializable
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

@Serializable
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
    val loader: ModLoaderType,
    val loaderMetadata: LoaderMetadata,
    val minMinecraftVersion: MinecraftVersion,
    val maxMinecraftVersion: MinecraftVersion,
    val pluginVersion: String,
) {
    val packageName: String
        get() = namespace.appendPackageName(id)

    val packagePath: String
        get() = packageName.setCase(`dot․case`, `path∕case`)

    val minecraftVersion: MinecraftVersion
        get() = minMinecraftVersion

    val versionRange: MinecraftVersionRange
        get() = minMinecraftVersion..maxMinecraftVersion

    val iconFileName: String
        get() = fileName("icon", Constants.File.Extension.PNG)

    val iconPath: String
        get() = iconFileName

    val configsDirectoryPath: String
        get() = JarConstants.Directory.META_INF

    val widenerConfigName: String
        get() = when (loader.family) {
            ModLoaderFamily.FABRIC -> fileName(id, "accesswidener")
            ModLoaderFamily.FORGE -> fileName("accesstransformer", "cfg")
        }

    val widenerConfigPath: String
        get() = configsDirectoryPath.appendPath(widenerConfigName)

    val mixinsConfigName: String
        get() = fileName(id, "mixins", Constants.File.Extension.JSON)

    val mixinsConfigPath: String
        get() = configsDirectoryPath.appendPath(mixinsConfigName)

    val resourcePackConfigName: String
        get() = fileName("pack", "mcmeta")

    val refmapFileName: String?
        get() = when (loader.family) {
            ModLoaderFamily.FABRIC -> fileName(id + "_refmap", Constants.File.Extension.JSON)
            ModLoaderFamily.FORGE -> null
        }

    val loaderConfigPath: String
        get() = when (loader.family) {
            ModLoaderFamily.FABRIC -> {
                fileName(
                    when (loader) {
                        ModLoaderType.QUILT -> loader.getName()
                        else -> ModLoaderFamily.FABRIC.getName()
                    },
                    "mod",
                    Constants.File.Extension.JSON
                )
            }

            ModLoaderFamily.FORGE -> {
                val fileName = when {
                    loader == ModLoaderType.NEOFORGE &&
                            minecraftVersion >= LoaderCompatibility.NeoForge.EPONYMOUS_MOD_CONFIG_NAME -> {
                        fileName(ModLoaderType.NEOFORGE.getName(), "mods", Constants.File.Extension.TOML)
                    }

                    else -> fileName("mods", Constants.File.Extension.TOML)
                }
                configsDirectoryPath.appendPath(fileName)
            }
        }

    val configEnvironment: String
        get() {
            val splitSide = if (environment == ModEnvironment.SERVER_ONLY) null else environment.sides.singleOrNull()
            return when (loader.family) {
                ModLoaderFamily.FABRIC -> splitSide?.getName() ?: Constants.Char.ASTERISK.toString()
                ModLoaderFamily.FORGE -> splitSide?.getName(SCREAMING_SNAKE_CASE) ?: "BOTH"
            }
        }

    val offlinePlayerUUID: UUID
        get() = UUID.nameUUIDFromBytes("OfflinePlayer:$player".toByteArray(Charsets.UTF_8))

    val jvmTarget: JvmTarget
        get() = minMinecraftVersion.minJavaVersion.toJvmTarget()

    val isReobfEnabled: Boolean =
        loader.family == ModLoaderFamily.FORGE && minecraftVersion < LoaderCompatibility.Forge.REOBF_CANCEL

    val archiveVersion: String
        get() = buildString {
            append(loader.getName())
            append(Constants.Char.HYPHEN)
            append(version)
            append(Constants.Char.PLUS)
            append(MinecraftConstants.SHORT_GAME_NAME)
            append(versionRange.asString())
        }

    fun getEntryPointName(side: ModSide): String =
        buildString {
            append(MinecraftConstants.FULL_GAME_NAME)
            if (loader == ModLoaderType.ORNITHE ||
                side == ModSide.CLIENT ||
                environment == ModEnvironment.DEDICATED_SERVER_ONLY
            ) {
                append(side.getName(PascalCase))
            }
            append("Mod")
        }

    fun log(project: Project, title: String, message: String? = null) {
        project.log(buildString {
            append(CrafterConstants.PLUGIN_NAME.wrapWithBrackets(BracketsType.SQUARE))
            append(Constants.Char.SPACE)
            append(
                buildString {
                    append(loader.displayName)
                    append(Constants.Char.SPACE)
                    append(Constants.Char.SLASH)
                    append(Constants.Char.SPACE)
                    append(versionRange.asString(Constants.Char.HYPHEN.toString()))
                }.wrapWithBrackets(BracketsType.SQUARE)
            )
            append(Constants.Char.SPACE)
            append(title)
            message?.let {
                appendLine()
                append(it)
            }
        })
    }
}
