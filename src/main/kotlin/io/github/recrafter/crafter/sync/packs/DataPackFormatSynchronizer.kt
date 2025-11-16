package io.github.recrafter.crafter.sync.packs

import io.github.recrafter.crafter.sync.packs.common.PackFormatSynchronizer

object DataPackFormatSynchronizer : PackFormatSynchronizer() {
    override val componentName: String = "data-pack-format"
    override val wikiTableCaption: String = "Data pack formats"
}
