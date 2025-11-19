import io.github.diskria.gradle.utils.extensions.children
import io.github.diskria.gradle.utils.extensions.getCatalogVersion
import io.github.diskria.gradle.utils.extensions.implementation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.projektor)
}

val includedProjects = buildList {
    add(project(":core"))
    addAll(project(":loaders").children)
}

dependencies {
    implementation(kotlin("gradle-plugin"))

    implementation(libs.bundles.loader.plugins)
    implementation(libs.bedrock)

    implementation(libs.kotlin.serialization.json)
    implementation(libs.java.poet)
    implementation(libs.jsoup)

    implementation(libs.bundles.diskria.utils)

    includedProjects.forEach {
        compileOnly(it)
    }
}

projekt {
    gradlePlugin {
        jvmTarget = JvmTarget.JVM_21
    }
}

tasks {
    jar {
        includedProjects.forEach {
            dependsOn(it.tasks.jar)
            from(it.sourceSets.map { it.output })
        }
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
