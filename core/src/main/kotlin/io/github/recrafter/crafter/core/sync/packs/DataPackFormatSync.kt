package io.github.recrafter.crafter.core.sync.packs

import io.github.recrafter.crafter.core.sync.packs.common.PackFormatSync

object DataPackFormatSync : PackFormatSync() {
    override val componentName: String = "data-pack-format"
    override val wikiTableCaption: String = "Data pack formats"
}
