package io.github.recrafter.crafter.sync.packs

import io.github.recrafter.crafter.sync.packs.common.PackFormatSynchronizer

object ResourcePackFormatSynchronizer : PackFormatSynchronizer() {
    override val componentName: String = "resource-pack-format"
    override val wikiTableCaption: String = "Resource pack formats"
}
