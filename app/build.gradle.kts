plugins {
    application
    id("repositories-conventions")
    id("kotlin-conventions")
    id("common-conventions")
}

dependencies {

    implementation(libs.axon.extensions.reactor.spring.boot.starter)
    implementation(libs.axon.messaging)
    implementation(libs.axon.modeling)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.springdoc.openapi.common)
    implementation(libs.springdoc.openapi.webflux.ui)
    implementation(libs.axon.spring.boot.starter)
    implementation(libs.spring.boot.starter.quartz)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.h2)
    implementation(libs.postgresql)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.c3p0)
    implementation(libs.coordinates)

    testImplementation(libs.axon.spring.boot.starter)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.reactor.test)
    testImplementation(libs.okhhtp3)
    testImplementation(libs.jackson.datatype)
    testImplementation(libs.h2)
    testImplementation(libs.axon.test)
}

application {
    mainClass.set("org.boris.BorisApplication")
}