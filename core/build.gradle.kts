plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(kotlin("gradle-plugin"))

    implementation(libs.bedrock)

    implementation(libs.kotlin.serialization.xml)
    implementation(libs.java.poet)
    implementation(libs.jsoup)
    implementation(libs.ktor)

    implementation(libs.bundles.diskria.utils)
}
