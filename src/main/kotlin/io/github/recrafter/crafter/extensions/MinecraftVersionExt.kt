package io.github.recrafter.crafter.extensions

import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.sync.packs.DataPackFormatSynchronizer
import io.github.recrafter.crafter.sync.packs.ResourcePackFormatSynchronizer
import org.gradle.api.Project

fun MinecraftVersion.getDataPackFormat(project: Project): String =
    DataPackFormatSynchronizer.getLatestVersion(project, this)

fun MinecraftVersion.getResourcePackFormat(project: Project): String =
    ResourcePackFormatSynchronizer.getLatestVersion(project, this)
