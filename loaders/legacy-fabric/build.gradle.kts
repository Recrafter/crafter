plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":loaders:fabric"))
    compileOnly(libs.legacy.fabric.plugin)

    implementation(libs.bedrock)

    implementation(libs.ktor)

    implementation(libs.bundles.diskria.utils)
}
