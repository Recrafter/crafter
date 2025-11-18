plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(kotlin("gradle-plugin"))

    compileOnly(project(":core"))
    compileOnly(project(":loaders:fabric"))

    implementation(libs.bedrock)
    compileOnly(libs.babric.plugin)

    implementation(libs.ktor)

    implementation(libs.bundles.diskria.utils)
}
