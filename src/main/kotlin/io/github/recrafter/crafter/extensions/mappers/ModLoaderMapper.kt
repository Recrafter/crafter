package io.github.recrafter.crafter.extensions.mappers

import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.loaders.ModLoaderType.*
import io.github.recrafter.crafter.babric.BabricModLoaderAdapter
import io.github.recrafter.crafter.core.ModLoaderAdapter
import io.github.recrafter.crafter.fabric.FabricModLoaderAdapter
import io.github.recrafter.crafter.forge.ForgeModLoaderAdapter
import io.github.recrafter.crafter.legacy_fabric.LegacyFabricModLoaderAdapter
import io.github.recrafter.crafter.neoforge.NeoForgeModLoaderAdapter
import io.github.recrafter.crafter.ornithe.OrnitheModLoaderAdapter
import io.github.recrafter.crafter.quilt.QuiltModLoaderAdapter

fun ModLoaderType.mapToModel(): ModLoaderAdapter =
    when (this) {
        FABRIC -> FabricModLoaderAdapter
        QUILT -> QuiltModLoaderAdapter
        LEGACY_FABRIC -> LegacyFabricModLoaderAdapter
        ORNITHE -> OrnitheModLoaderAdapter
        BABRIC -> BabricModLoaderAdapter
        FORGE -> ForgeModLoaderAdapter
        NEOFORGE -> NeoForgeModLoaderAdapter
    }
