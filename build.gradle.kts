plugins {
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.ben-manes.versions") version "0.54.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.spring") version "2.4.0"
}

group = "no.novari"
version = "0.0.1-SNAPSHOT"

var fintResourceModelVersion = "4.0.10"

kotlin {
    jvmToolchain(25)
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
    mavenLocal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("no.novari:flyt-kafka:7.1.0")
    implementation("no.novari:flyt-cache:3.0.0")
    implementation("no.novari:flyt-web-resource-server:3.1.0")

    implementation("no.novari:fint-arkiv-resource-model-java:$fintResourceModelVersion")
    implementation("no.novari:fint-administrasjon-resource-model-java:$fintResourceModelVersion")

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.1.0")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--sun-misc-unsafe-memory-access=allow")
}

tasks.bootRun {
    jvmArgs("--sun-misc-unsafe-memory-access=allow")
}

tasks.jar {
    isEnabled = false
}

ktlint {
    version.set("1.8.0")
}

tasks.named("check") {
    dependsOn("ktlintCheck")
}
