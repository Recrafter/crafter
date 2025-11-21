package io.github.recrafter.crafter.core.extensions

import io.github.recrafter.bedrock.era.Beta
import io.github.recrafter.bedrock.era.Release
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.loaders.ModLoaderType.*
import io.github.recrafter.bedrock.sides.ServerType
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.MinecraftVersionRange
import io.github.recrafter.bedrock.versions.rangeTo

val ModLoaderType.supportedVersionRange: MinecraftVersionRange
    get() = when (this) {
        FABRIC -> Release.V_1_14_4..MinecraftVersion.LATEST
        QUILT -> Release.V_1_18_2..MinecraftVersion.LATEST
        LEGACY_FABRIC -> ServerType.INTEGRATED.startMinecraftVersion..Release.V_1_13_2
        BABRIC -> MinecraftVersionRange(Beta.B_1_7_3)
        ORNITHE -> MinecraftVersion.EARLIEST..Release.V_1_13_2
        FORGE -> Release.V_1_17_1..MinecraftVersion.LATEST
        NEOFORGE -> Release.V_1_20_2..MinecraftVersion.LATEST
    }
