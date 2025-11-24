package io.github.recrafter.crafter.core

import io.github.diskria.gradle.utils.extensions.common.requireGradleNotNull
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.recrafter.bedrock.crafter.CrafterConstants

object CrafterTasks {

    const val PUBLIC_TASKS_GROUP: String = CrafterConstants.PLUGIN_NAME
    val INTERNAL_TASKS_GROUP: String = PUBLIC_TASKS_GROUP.appendPath("internal")

    val pluginVersion: String
        get() = requireGradleNotNull(this::class.java.`package`?.implementationVersion) {
            "Failed to get plugin version."
        }
}
