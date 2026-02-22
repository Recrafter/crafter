plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(libs.forge.plugin)
    compileOnly(libs.forge.jarjar.plugin)

    implementation(libs.bedrock)

    implementation(libs.ktor)

    implementation(libs.bundles.diskria.utils)
}
