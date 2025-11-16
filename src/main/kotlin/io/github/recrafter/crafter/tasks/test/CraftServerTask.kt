package io.github.recrafter.crafter.tasks.test

import io.github.recrafter.crafter.CrafterGradlePlugin
import org.gradle.api.DefaultTask

abstract class CraftServerTask : DefaultTask() {

    init {
        group = CrafterGradlePlugin.PLUGIN_NAME
    }
}
