plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(kotlin("gradle-plugin"))

    compileOnly(project(":core"))

    implementation(libs.bedrock)

    implementation(libs.kotlin.serialization.json)

    implementation(libs.bundles.diskria.utils)
}
