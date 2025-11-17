package io.github.recrafter.crafter.core.tasks.test

import io.github.recrafter.crafter.core.CrafterConstants
import org.gradle.api.DefaultTask

abstract class CraftServerTask : DefaultTask() {

    init {
        group = CrafterConstants.TASK_GROUP
    }
}
