plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)

    compileOnly(project(":core"))
    compileOnly(project(":loaders:fabric"))
    compileOnly(libs.ornithe.plugin)

    implementation(libs.bedrock)

    implementation(libs.ktor)

    implementation(libs.bundles.diskria.utils)
}
