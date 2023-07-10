plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.plugin)
    implementation(libs.spring.boot.plugin)
}

repositories {
    gradlePluginPortal()
}