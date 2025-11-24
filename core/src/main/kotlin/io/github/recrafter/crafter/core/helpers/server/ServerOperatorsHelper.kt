package io.github.recrafter.crafter.core.helpers.server

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.diskria.kotlin.utils.extensions.serialization.serializeToJson
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.helpers.PresetHelper
import io.github.recrafter.crafter.core.helpers.server.operators.OperatorEntry
import io.github.recrafter.crafter.core.helpers.server.operators.Operators

object ServerOperatorsHelper : PresetHelper() {

    const val MAX_LEVEL: Int = 4

    val FILE_NAME: String = fileName("ops", Constants.File.Extension.JSON)

    override fun buildPreset(mod: Mod): String =
        Operators(
            listOf(
                OperatorEntry(
                    uuid = mod.offlinePlayerUUID.toString(),
                    name = mod.player,
                    level = MAX_LEVEL,
                    canBypassPlayerLimit = true,
                )
            )
        ).serializeToJson()
}
