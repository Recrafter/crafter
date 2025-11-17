package io.github.recrafter.crafter.babric

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.babric.extensions.babric
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.ModLoader
import org.gradle.api.Project
import java.io.File

object BabricModLoader : ModLoader {

    override fun configurePlugin(
        mod: Mod,
        project: Project,
        sides: Set<ModSide>,
        accessConfig: File,
    ) = with(project) {
        gradleError("Babric mod loader is unsupported yet")
        babric {

        }
    }
}
