import io.github.diskria.gradle.utils.extensions.common.gradleProjectPath
import io.github.diskria.gradle.utils.extensions.rootDirectory
import io.github.diskria.kotlin.utils.extensions.listDirectories
import io.github.diskria.projektor.common.licenses.LicenseType.MIT
import io.github.diskria.projektor.common.publishing.PublishingTargetType.GITHUB_PAGES

pluginManagement {
    repositories {
        fun resolvePluginMaven(repoName: String) {
            val mavenName = repoName.replaceFirstChar { it.uppercaseChar() }
            val localMavens = rootDir.parentFile
                .resolve(repoName).resolve("build/maven").listFiles().orEmpty()
            if (localMavens.isNotEmpty()) {
                maven(uri(localMavens.first())) {
                    name = "$mavenName Local"
                }
            } else {
                maven("https://recrafter.github.io/$repoName") {
                    name = mavenName
                }
            }
        }

        maven("https://diskria.github.io/projektor") {
            name = "Projektor"
        }
        resolvePluginMaven("recipe")

        gradlePluginPortal()
    }
}

plugins {
    id("io.github.diskria.projektor.settings") version "4.+"
    id("io.github.recrafter.recipe") version "0.2.8"
}

projekt {
    version = "0.3.4"
    license = MIT
    publish = setOf(
        GITHUB_PAGES,
    )

    gradlePlugin()
}

recipe {
    crafter {
        craftingCrafters()
    }
}

include(":core")
include(":cli")
val loadersDirectoryName = "loaders"
rootDirectory.resolve(loadersDirectoryName).listDirectories().forEach {
    include(gradleProjectPath(loadersDirectoryName, it.name))
}
