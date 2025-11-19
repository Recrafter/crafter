package io.github.recrafter.crafter.core

import io.github.diskria.kotlin.utils.extensions.appendPath

object CrafterConstants {
    const val PLUGIN_NAME: String = "Crafter"
    const val PUBLIC_TASKS_GROUP: String = PLUGIN_NAME

    val PLUGIN_LOWER_NAME: String = PLUGIN_NAME.lowercase()
    val INTERNAL_TASKS_GROUP: String = PUBLIC_TASKS_GROUP.appendPath("internal")
}
