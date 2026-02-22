plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    compileOnly(project(":core"))

    implementation(libs.bedrock)

    implementation(libs.kotlin.serialization.json)

    implementation(libs.bundles.diskria.utils)
}
