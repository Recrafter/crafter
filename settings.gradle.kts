import io.github.diskria.gradle.utils.extensions.common.buildGradleProjectPath
import io.github.diskria.gradle.utils.extensions.rootDirectory
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.listDirectories
import io.github.diskria.projektor.common.licenses.LicenseType.MIT
import io.github.diskria.projektor.common.publishing.PublishingTargetType.GITHUB_PAGES
import io.github.diskria.projektor.settings.extensions.configureMaven
import io.ktor.http.*

pluginManagement {
    repositories {
        maven("https://diskria.github.io/projektor") {
            name = "Projektor"
        }
        gradlePluginPortal()
    }
}

plugins {
    id("io.github.diskria.projektor.settings") version "4.+"
}

projekt {
    version = "0.2.3"
    license = MIT
    publish = setOf(
        GITHUB_PAGES,
    )

    gradlePlugin()
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        configureMaven(
            name = "Quilt",
            url = buildUrl("maven.quiltmc.org") {
                path("repository", "release")
            }
        )
        configureMaven(
            name = "Babric",
            url = buildUrl("maven.glass-launcher.net") {
                path("babric")
            }
        )
    }
}

include(":core")
val loadersDirectoryName = "loaders"
rootDirectory.resolve(loadersDirectoryName).listDirectories().forEach {
    include(buildGradleProjectPath(loadersDirectoryName, it.name))
}
