package io.github.recrafter.crafter.core.helpers.server

import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.helpers.PresetHelper

object ServerPropertiesHelper : PresetHelper() {

    val FILE_NAME: String = fileName(ModSide.SERVER.getName(), Constants.File.Extension.PROPERTIES)

    override fun buildPreset(mod: Mod): String =
        buildString {
            appendLine(buildArgument("online-mode", false))
            appendLine(buildArgument("allow-flight", true))
            appendLine(buildArgument("enforce-secure-profile", false))
            appendLine(buildArgument("op-permission-level", ServerOperatorsHelper.MAX_LEVEL))
        }
}
