package io.github.recrafter.crafter.core

import io.github.diskria.kotlin.utils.extensions.appendPath

object CrafterConstants {

    const val PLUGIN_NAME: String = "crafter"
    const val TASKS_CATEGORY: String = PLUGIN_NAME

    val INTERNAL_TASKS_CATEGORY: String = TASKS_CATEGORY.appendPath("internal")
}
