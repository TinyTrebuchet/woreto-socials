plugins {
    id("java")
    id("org.springframework.boot") version "3.4.3"
}

apply(plugin = "io.spring.dependency-management")

group = "com.woreto"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.seleniumhq.selenium:selenium-java:4.29.0")
    implementation("org.seleniumhq.selenium:selenium-devtools-v131:4.29.0")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}