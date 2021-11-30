plugins {
    kotlin("jvm") version "1.6.0"
}

group = "io.orange-buffalo"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val integrationsDependencies by configurations.creating {
    isTransitive = false
}

configurations {
    compileClasspath.get().extendsFrom(integrationsDependencies)
    testCompileClasspath.get().extendsFrom(integrationsDependencies)
}

dependencies {
    api(kotlin("stdlib"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.2")

    integrationsDependencies("com.fasterxml.jackson.core:jackson-core:2.13.0")

    testImplementation("io.kotest:kotest-runner-junit5:4.6.3")
    testImplementation("io.mockk:mockk:1.12.1")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
