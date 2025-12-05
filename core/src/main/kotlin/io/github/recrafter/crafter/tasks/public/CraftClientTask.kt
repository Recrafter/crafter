package io.github.recrafter.crafter.tasks.public

import io.github.recrafter.crafter.core.CrafterTasks
import org.gradle.api.DefaultTask

abstract class CraftClientTask : DefaultTask() {

    init {
        group = CrafterTasks.PUBLIC_TASKS_GROUP
    }
}
