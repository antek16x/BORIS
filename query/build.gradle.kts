plugins {
    `java-library`
    id("common-conventions")
}

dependencies {
    implementation(project(":core-api"))

    implementation(libs.axon.messaging)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.h2)
    implementation(libs.postgresql)

    testImplementation(project(":shared"))

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.h2)
    testImplementation(libs.axon.test)
}