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
                    name = "$mavenName-Local"
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

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        maven("https://jitpack.io")
    }
}

plugins {
    id("io.github.diskria.projektor.settings") version "5.+"
    id("io.github.recrafter.recipe") version "1.2.0"
}

projekt {
    version = "1.2.0"
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

include(":core", ":cli")
rootDirectory.resolve("loaders").listDirectories().forEach {
    include(gradleProjectPath(it.parentFile.name, it.name))
}
