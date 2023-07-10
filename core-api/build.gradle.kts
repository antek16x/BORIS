plugins {
    `java-library`
    id("common-conventions")
    id("kotlin-conventions")
}

dependencies {
    implementation(libs.axon.modeling)
    implementation(libs.spring.boot)
    implementation(libs.springdoc.openapi.common)
}