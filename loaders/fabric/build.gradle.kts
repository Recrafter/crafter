plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(rootProject)
    implementation(libs.fabric.plugin)

    implementation(libs.bundles.diskria.utils)
}
