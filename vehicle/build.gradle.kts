plugins {
    `java-library`
    id("common-conventions")
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":shared"))

    implementation(libs.axon.extensions.reactor.spring.boot.starter)
    implementation(libs.axon.messaging)
    implementation(libs.axon.modeling)
    implementation(libs.axon.spring.boot.starter)
    implementation(libs.spring.boot.starter.quartz)

    testImplementation(libs.axon.spring.boot.starter)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.axon.test)
}