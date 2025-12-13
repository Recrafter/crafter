package io.github.recrafter.crafter.core

import io.github.recrafter.bedrock.era.Release
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.loaders.ModLoaderType.FORGE
import io.github.recrafter.bedrock.loaders.ModLoaderType.NEOFORGE
import io.github.recrafter.bedrock.versions.JavaCompatibility
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.core.extensions.supportedVersions

object LoaderCompatibility {

    fun getCheckpoints(loader: ModLoaderType): List<MinecraftVersion> {
        val checkpoints = mutableListOf<MinecraftVersion>()
        loader.supportedVersions.windowed(2).forEach { (previous, next) ->
            val previousMinJavaVersion = JavaCompatibility.getMinJavaVersion(previous)
            val nextMinJavaVersion = JavaCompatibility.getMinJavaVersion(next)
            if (nextMinJavaVersion > previousMinJavaVersion) {
                checkpoints += next
            }
        }
        checkpoints += when (loader) {
            FORGE -> listOf(Forge.REOBF_CANCEL, Forge.PACK_CONFIG_RANGES)
            NEOFORGE -> listOf(NeoForge.EPONYMOUS_MOD_CONFIG_NAME)
            else -> emptyList()
        }
        return checkpoints.toSortedSet(MinecraftVersion.COMPARATOR).toList()
    }

    object Forge {
        val REOBF_CANCEL: MinecraftVersion = Release.V_1_20_6
        val PACK_CONFIG_RANGES: MinecraftVersion = Release.V_1_21_9
    }

    object NeoForge {
        val EPONYMOUS_MOD_CONFIG_NAME: MinecraftVersion = Release.V_1_20_5
    }
}
