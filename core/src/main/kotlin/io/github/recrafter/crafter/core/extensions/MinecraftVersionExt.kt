package io.github.recrafter.crafter.core.extensions

import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.crafter.core.sync.packs.DataPackFormatSync
import io.github.recrafter.crafter.core.sync.packs.ResourcePackFormatSync
import org.gradle.api.Project

fun MinecraftVersion.getDataPackFormat(project: Project): String =
    DataPackFormatSync.getLatestVersion(project, this)

fun MinecraftVersion.getResourcePackFormat(project: Project): String =
    ResourcePackFormatSync.getLatestVersion(project, this)
