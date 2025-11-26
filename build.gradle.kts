plugins {
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    java
    id("com.github.ben-manes.versions") version "0.53.0"
}

group = "no.fintlabs"
version = "0.0.1-SNAPSHOT"

var fintResourceModelVersion = "3.21.10"
var findModelResourceVersion = "0.5.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
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
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.kafka:spring-kafka")

    implementation("io.projectreactor.addons:reactor-extra")

    implementation("no.novari:flyt-kafka:4.0.0-rc-10")
    implementation("no.novari:flyt-cache:2.0.0-rc-2")
    implementation("no.novari:flyt-resource-server:6.0.0-rc-27")

    implementation("no.fintlabs:fint-model-resource:$findModelResourceVersion")
    implementation("no.fint:fint-arkiv-resource-model-java:$fintResourceModelVersion")
    implementation("no.fint:fint-administrasjon-resource-model-java:$fintResourceModelVersion")

    compileOnly("org.projectlombok:lombok")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    isEnabled = false
}
