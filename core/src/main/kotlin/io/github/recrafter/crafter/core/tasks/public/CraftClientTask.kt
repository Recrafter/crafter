package io.github.recrafter.crafter.core.tasks.public

import io.github.recrafter.crafter.core.CrafterConstants
import org.gradle.api.DefaultTask

abstract class CraftClientTask : DefaultTask() {

    init {
        group = CrafterConstants.PUBLIC_TASKS_GROUP
    }
}
