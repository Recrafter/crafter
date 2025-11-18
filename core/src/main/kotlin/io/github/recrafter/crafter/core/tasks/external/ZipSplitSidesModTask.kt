package io.github.recrafter.crafter.core.tasks.external

import io.github.diskria.gradle.utils.helpers.GradleDirectories
import org.gradle.api.tasks.bundling.Zip

abstract class ZipSplitSidesModTask : Zip() {

    init {
        group = GradleDirectories.BUILD
    }
}