package io.github.recrafter.crafter.tasks

import io.github.recrafter.crafter.core.CrafterTasks
import org.gradle.api.DefaultTask

abstract class CraftClientTask : DefaultTask() {

    init {
        group = CrafterTasks.PUBLIC_GROUP
    }
}
