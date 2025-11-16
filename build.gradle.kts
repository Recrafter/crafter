import io.github.diskria.gradle.utils.extensions.children
import io.github.diskria.gradle.utils.extensions.getCatalogVersion
import io.github.diskria.gradle.utils.extensions.main
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    alias(libs.plugins.projektor)
}

dependencies {
    implementation(libs.kotlin.jvm.plugin)
    implementation(libs.kotlin.html)
    implementation(libs.kotlin.serialization.xml)
    implementation(libs.jsoup)
    implementation(libs.java.poet)

    implementation(libs.bundles.diskria.utils)
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.loader.plugins)

    implementation(libs.bedrock)
}

projekt {
    gradlePlugin {
        jvmTarget = JvmTarget.JVM_21
    }
}

tasks {
    jar {
        dependsOn(project(":loaders").children.map { it.tasks.jar })
        from(project(":loaders").children.map { it.sourceSets.main.output })
    }
}

val kotlinVersion = getCatalogVersion("kotlin")
configurations.all {
    resolutionStrategy {
        eachDependency {
            when (requested.group) {
                "org.jetbrains.kotlin" -> useVersion(kotlinVersion)
            }
        }
    }
}
