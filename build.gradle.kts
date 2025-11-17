import io.github.diskria.gradle.utils.extensions.children
import io.github.diskria.gradle.utils.extensions.getCatalogVersion
import io.github.diskria.gradle.utils.extensions.main
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.projektor)
}

val includedModules = buildList {
    add(project(":core"))
    addAll(project(":loaders").children)
}

dependencies {
    implementation(libs.kotlin.jvm.plugin)
    implementation(libs.kotlin.serialization.xml)
    implementation(libs.jsoup)
    implementation(libs.java.poet)

    implementation(libs.bundles.diskria.utils)
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.implementation.loader.plugins)

    implementation(libs.bedrock)

    includedModules.forEach { compileOnly(it) }
}

projekt {
    gradlePlugin {
        jvmTarget = JvmTarget.JVM_21
    }
}

tasks {
    jar {
        includedModules.forEach {
            dependsOn(it.tasks.jar)
            from(it.sourceSets.main.output)
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
