package io.github.recrafter.crafter.extensions.mappers

import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.loaders.ModLoaderType.*
import io.github.recrafter.crafter.babric.BabricModLoader
import io.github.recrafter.crafter.core.ModLoader
import io.github.recrafter.crafter.fabric.FabricModLoader
import io.github.recrafter.crafter.forge.ForgeModLoader
import io.github.recrafter.crafter.legacy_fabric.LegacyFabricModLoader
import io.github.recrafter.crafter.neoforge.NeoForgeModLoader
import io.github.recrafter.crafter.ornithe.OrnitheModLoader
import io.github.recrafter.crafter.quilt.QuiltModLoader

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
