plugins {
    `java-library`
    id("common-conventions")
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":query"))

    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.axon.spring.boot.starter)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.coordinates)
    implementation(libs.c3p0)
    implementation(libs.postgresql)
    implementation(libs.spring.boot.starter.quartz)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.data.jpa)
    testImplementation(libs.okhhtp3)
    testImplementation(libs.jackson.datatype)
    testImplementation(libs.reactor.test)
}