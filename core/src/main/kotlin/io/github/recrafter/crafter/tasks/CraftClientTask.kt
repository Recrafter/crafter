package io.github.recrafter.crafter.tasks

import io.github.recrafter.crafter.core.CrafterTasks
import org.gradle.api.DefaultTask
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Runs an interactive client process")
abstract class CraftClientTask : DefaultTask() {

    init {
        group = CrafterTasks.PUBLIC_GROUP
    }
}
