plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlin.serialization.xml)

    implementation(libs.bedrock)
    implementation(libs.bundles.diskria.utils)
    implementation(libs.bundles.ktor.client)

    compileOnly(libs.quilt.plugin)
    compileOnly(libs.legacy.fabric.plugin)
    compileOnly(libs.ornithe.plugin)

    compileOnly(project(":core"))
}
