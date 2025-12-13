package io.github.recrafter.crafter.core.extensions

import io.github.recrafter.bedrock.era.Beta
import io.github.recrafter.bedrock.era.Release
import io.github.recrafter.bedrock.loaders.ModLoaderFamily
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.loaders.ModLoaderType.*
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.MinecraftVersionRange
import io.github.recrafter.bedrock.versions.rangeTo
import io.github.recrafter.crafter.core.mixins.accessors.wideners.format.AccessTransformer
import io.github.recrafter.crafter.core.mixins.accessors.wideners.format.AccessWidener
import io.github.recrafter.crafter.core.mixins.accessors.wideners.format.WidenerConfigFormat

val ModLoaderType.supportedVersionRanges: List<MinecraftVersionRange>
    get() = when (this) {
        FABRIC -> listOf(Release.V_1_14_4..MinecraftVersion.LATEST)
        QUILT -> listOf(Release.V_1_18_2..MinecraftVersion.LATEST)
        LEGACY_FABRIC -> listOf(
            Release.V_1_3_1..Release.V_1_7_8,
            Release.V_1_7_10..Release.V_1_8_9,
            MinecraftVersionRange.of(Release.V_1_9_4),
            MinecraftVersionRange.of(Release.V_1_10_2),
            MinecraftVersionRange.of(Release.V_1_11_2),
            MinecraftVersionRange.of(Release.V_1_12_2),
            MinecraftVersionRange.of(Release.V_1_13_2),
        )

        BABRIC -> listOf(MinecraftVersionRange.of(Beta.B_1_7_3))
        ORNITHE -> listOf(MinecraftVersion.EARLIEST..Release.V_1_8_9)
        FORGE -> listOf(Release.V_1_17_1..MinecraftVersion.LATEST)
        NEOFORGE -> listOf(Release.V_1_20_4..MinecraftVersion.LATEST)
    }

val ModLoaderType.supportedVersions: List<MinecraftVersion>
    get() = supportedVersionRanges.flatMap { it.expand() }.toSortedSet(MinecraftVersion.COMPARATOR).toList()

val ModLoaderType.family: ModLoaderFamily
    get() = ModLoaderFamily.of(this)

val ModLoaderType.widenerConfigFormat: WidenerConfigFormat
    get() = when (family) {
        ModLoaderFamily.FABRIC -> AccessWidener
        ModLoaderFamily.FORGE -> AccessTransformer
    }

