package io.github.recrafter.crafter.extensions.mappers

import io.github.diskria.kotlin.utils.extensions.common.failWithUnsupportedType
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.loaders.ModLoaderType.*
import io.github.recrafter.crafter.loaders.common.ModLoader
import io.github.recrafter.crafter.loaders.fabric.*
import io.github.recrafter.crafter.loaders.forge.ForgeModLoader
import io.github.recrafter.crafter.loaders.forge.NeoForgeModLoader

fun ModLoaderType.mapToModel(): ModLoader =
    when (this) {
        FABRIC -> FabricModLoader
        QUILT -> QuiltModLoader
        LEGACY_FABRIC -> LegacyFabricModLoader
        ORNITHE -> OrnitheModLoader
        BABRIC -> BabricModLoader
        FORGE -> ForgeModLoader
        NEOFORGE -> NeoForgeModLoader
    }

fun ModLoader.mapToEnum(): ModLoaderType =
    when (this) {
        FabricModLoader -> FABRIC
        QuiltModLoader -> QUILT
        LegacyFabricModLoader -> LEGACY_FABRIC
        OrnitheModLoader -> ORNITHE
        BabricModLoader -> BABRIC
        ForgeModLoader -> FORGE
        NeoForgeModLoader -> NEOFORGE
        else -> failWithUnsupportedType(this::class)
    }
