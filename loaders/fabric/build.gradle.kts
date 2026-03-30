plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)

    compileOnly(project(":core"))
    compileOnly(libs.quilt.remap.plugin)
    compileOnly(libs.quilt.plugin)
    compileOnly(libs.shadow.plugin)
    compileOnly(libs.bundles.lapis)

    implementation(libs.bedrock)

    implementation(libs.ktor)

    implementation(libs.bundles.diskria.utils)
}
