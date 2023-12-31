plugins {
    application
    id("repositories-conventions")
    id("kotlin-conventions")
    id("common-conventions")
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":shared"))
    implementation(project(":query"))
    implementation(project(":vehicle"))

    implementation(libs.axon.extensions.reactor.spring.boot.starter)
    implementation(libs.axon.messaging)
    implementation(libs.axon.modeling)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.springdoc.openapi.common)
    implementation(libs.springdoc.openapi.webflux.ui)
    implementation(libs.h2)

    testImplementation(libs.axon.spring.boot.starter)
    testImplementation(libs.spring.boot.starter.test)
}

application {
    mainClass.set("org.boris.BorisApplication")
}