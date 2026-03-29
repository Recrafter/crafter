import io.github.diskria.gradle.utils.extensions.common.gradleProjectPath
import io.github.diskria.gradle.utils.extensions.rootDirectory
import io.github.diskria.kotlin.utils.extensions.listDirectories
import io.github.diskria.projektor.common.licenses.LicenseType.MIT
import io.github.diskria.projektor.common.publishing.PublishingTargetType
import io.github.diskria.projektor.common.publishing.PublishingTargetType.GRADLE_PLUGIN_PORTAL

pluginManagement {
    repositories {
        maven("https://diskria.github.io/projektor") {
            name = "Projektor"
        }
        gradlePluginPortal()
    }
}

plugins {
    id("io.github.diskria.projektor.settings") version "6.+"
    id("io.github.recrafter.recipe") version "1.2.6"
}

projekt {
    version = "1.2.4"
    license = MIT
    publish = setOf(GRADLE_PLUGIN_PORTAL)

    gradlePlugin()
}

recipe {
    crafter {
        mavensOnly()
    }
}

include(":core", ":cli")
rootDirectory.resolve("loaders").listDirectories().forEach {
    include(gradleProjectPath(it.parentFile.name, it.name))
}
