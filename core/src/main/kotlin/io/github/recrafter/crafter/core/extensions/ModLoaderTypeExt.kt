package io.github.recrafter.crafter.core.extensions

import io.github.recrafter.bedrock.era.Beta
import io.github.recrafter.bedrock.era.FullRelease
import io.github.recrafter.bedrock.loaders.ModLoaderFamily
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.loaders.ModLoaderType.*
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.MinecraftVersionRange
import io.github.recrafter.bedrock.versions.rangeTo

val ModLoaderType.supportedVersionRanges: List<MinecraftVersionRange>
    get() = when (this) {
        FABRIC -> listOf(FullRelease.V_1_14_4..MinecraftVersion.LATEST)
        QUILT -> listOf(FullRelease.V_1_18_2..MinecraftVersion.LATEST)
        LEGACY_FABRIC -> listOf(
            FullRelease.V_1_3_1..FullRelease.V_1_7_8,
            FullRelease.V_1_7_10..FullRelease.V_1_8_9,
            MinecraftVersionRange.of(FullRelease.V_1_9_4),
            MinecraftVersionRange.of(FullRelease.V_1_10_2),
            MinecraftVersionRange.of(FullRelease.V_1_11_2),
            MinecraftVersionRange.of(FullRelease.V_1_12_2),
            MinecraftVersionRange.of(FullRelease.V_1_13_2),
        )

        BABRIC -> listOf(MinecraftVersionRange.of(Beta.B_1_7_3))
        ORNITHE -> listOf(MinecraftVersion.EARLIEST..FullRelease.V_1_8_9)
        FORGE -> listOf(
            FullRelease.V_1_19..FullRelease.V_1_20_4,
            FullRelease.V_1_20_6..FullRelease.V_1_21_1,
            FullRelease.V_1_21_3..MinecraftVersion.LATEST,
        )

        NEOFORGE -> listOf(FullRelease.V_1_20_4..MinecraftVersion.LATEST)
    }

val ModLoaderType.supportedVersions: List<MinecraftVersion>
    get() = supportedVersionRanges.flatMap { it.expand() }.toSortedSet(MinecraftVersion.COMPARATOR).toList()

val ModLoaderType.family: ModLoaderFamily
    get() = ModLoaderFamily.of(this)
