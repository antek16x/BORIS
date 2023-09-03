plugins {
    application
    id("kotlin-conventions")
    id("common-conventions")
}

dependencies {
    implementation(libs.axon.modeling)
    implementation(libs.springdoc.openapi.common)
}