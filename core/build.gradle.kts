import io.github.diskria.gradle.utils.extensions.implementation

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(kotlin("gradle-plugin"))

    implementation(libs.bedrock)

    compileOnly(libs.bundles.lapis)

    compileOnly(libs.shadow.plugin)
    compileOnly(libs.ksp.plugin)

    implementation(libs.kotlin.serialization.xml)
    implementation(libs.java.poet)
    implementation(libs.jsoup)
    implementation(libs.ktor)

    implementation(libs.bundles.diskria.utils)
}
