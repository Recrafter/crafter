package io.github.recrafter.crafter.core.helpers.server

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.helpers.PresetHelper

object EulaHelper : PresetHelper() {

    private const val EULA_NAME: String = "eula"

    val FILE_NAME: String = fileName(EULA_NAME, Constants.File.Extension.TXT)

    override fun buildPreset(mod: Mod): String =
        buildString {
            appendLine(buildArgument(EULA_NAME, true))
        }
}
