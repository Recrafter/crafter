plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(libs.kotlin.serialization.xml)

    implementation(libs.bedrock)
    implementation(libs.bundles.diskria.utils)
    implementation(libs.bundles.ktor.client)

    compileOnly(libs.neoforge.plugin)

    compileOnly(project(":core"))
}
