plugins {
    application
    id("java")
    kotlin("jvm") version "1.9.0"
}

group = "boris"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core-api"))

    implementation("org.axonframework:axon-messaging:4.8.0")
    implementation("org.springdoc:springdoc-openapi-common:1.7.0")
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.1.1")
    implementation("org.axonframework.extensions.reactor:axon-reactor-spring-boot-starter:4.8.0")

    testImplementation("org.axonframework:axon-spring-boot-starter:4.8.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.1.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))
}

application {
    mainClass.set("org.boris.BorisApplication")
}