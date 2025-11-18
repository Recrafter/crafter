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

    compileOnly(project(":core"))
    compileOnly(project(":loaders:fabric"))
}
