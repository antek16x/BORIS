import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    java
    id("org.springframework.boot")
    id("repositories-conventions")
}
group = "boris"

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.slf4j") {
            useVersion("1.7.36")
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.getByName<Jar>("bootJar") {
    enabled = false
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}