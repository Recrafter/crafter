package io.github.recrafter.crafter.core

import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.recrafter.bedrock.crafter.CrafterConstants

object CrafterTasks {
    const val PUBLIC_GROUP: String = CrafterConstants.PLUGIN_NAME
    val INTERNAL_GROUP: String = PUBLIC_GROUP.appendPath("internal")
}
