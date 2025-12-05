package io.github.recrafter.crafter.extensions.mappers

import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.loaders.ModLoaderType.*
import io.github.recrafter.crafter.core.ModLoaderAdapter
import io.github.recrafter.crafter.loaders.babric.BabricAdapter
import io.github.recrafter.crafter.loaders.fabric.FabricAdapter
import io.github.recrafter.crafter.loaders.forge.ForgeAdapter
import io.github.recrafter.crafter.loaders.legacy_fabric.LegacyFabricAdapter
import io.github.recrafter.crafter.loaders.neoforge.NeoForgeAdapter
import io.github.recrafter.crafter.loaders.ornithe.OrnitheAdapter
import io.github.recrafter.crafter.loaders.quilt.QuiltAdapter

fun ModLoaderType.mapToAdapter(): ModLoaderAdapter =
    when (this) {
        FABRIC -> FabricAdapter
        QUILT -> QuiltAdapter
        LEGACY_FABRIC -> LegacyFabricAdapter
        ORNITHE -> OrnitheAdapter
        BABRIC -> BabricAdapter
        FORGE -> ForgeAdapter
        NEOFORGE -> NeoForgeAdapter
    }
