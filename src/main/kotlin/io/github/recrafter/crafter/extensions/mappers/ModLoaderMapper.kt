package io.github.recrafter.crafter.extensions.mappers

import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.loaders.ModLoaderType.*
import io.github.recrafter.crafter.babric.BabricAdapter
import io.github.recrafter.crafter.core.ModLoaderAdapter
import io.github.recrafter.crafter.fabric.FabricAdapter
import io.github.recrafter.crafter.forge.ForgeAdapter
import io.github.recrafter.crafter.legacy_fabric.LegacyFabricAdapter
import io.github.recrafter.crafter.neoforge.NeoForgeAdapter
import io.github.recrafter.crafter.ornithe.OrnitheAdapter
import io.github.recrafter.crafter.quilt.QuiltAdapter

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
