plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(libs.kotlin.serialization.xml)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.jvm.plugin)
    implementation(libs.jsoup)
    implementation(libs.java.poet)

    implementation(libs.bedrock)
    implementation(libs.bundles.diskria.utils)
    implementation(libs.bundles.ktor.client)
}
