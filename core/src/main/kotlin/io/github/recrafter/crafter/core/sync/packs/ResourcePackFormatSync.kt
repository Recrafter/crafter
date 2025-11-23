package io.github.recrafter.crafter.core.sync.packs

import io.github.recrafter.crafter.core.sync.packs.common.PackFormatSync

object ResourcePackFormatSync : PackFormatSync() {
    override val componentName: String = "resource-pack-format"
    override val wikiTableCaption: String = "Resource pack formats"
}
