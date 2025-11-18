plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(kotlin("gradle-plugin"))

    compileOnly(project(":core"))

    implementation(libs.bedrock)
    compileOnly(libs.forge.plugin)

    implementation(libs.ktor)

    implementation(libs.bundles.diskria.utils)
}
