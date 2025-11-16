package io.github.recrafter.crafter.loaders.fabric

import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.loaders.common.ModLoader
import io.github.recrafter.crafter.models.Mod
import org.gradle.api.Project
import java.io.File

object BabricModLoader : ModLoader() {

    override fun configurePlugin(
        mod: Mod,
        project: Project,
        sides: Set<ModSide>,
        accessConfig: File,
    ) = with(project) {

    }
}