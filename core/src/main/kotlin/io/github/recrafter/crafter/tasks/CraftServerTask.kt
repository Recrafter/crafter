package io.github.recrafter.crafter.tasks

import io.github.recrafter.crafter.core.CrafterTasks
import org.gradle.api.DefaultTask
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Starts an interactive server process with side effects")
abstract class CraftServerTask : DefaultTask() {

    init {
        group = CrafterTasks.PUBLIC_GROUP
    }
}
