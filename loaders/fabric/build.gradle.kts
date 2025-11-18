plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(kotlin("gradle-plugin"))

    compileOnly(project(":core"))
    compileOnly(libs.quilt.plugin)
    compileOnly(libs.legacy.fabric.plugin)
    compileOnly(libs.ornithe.plugin)

    implementation(libs.bedrock)

    implementation(libs.ktor)

    implementation(libs.bundles.diskria.utils)
}
