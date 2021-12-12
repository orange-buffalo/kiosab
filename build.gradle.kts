plugins {
    kotlin("jvm") version "1.6.0"
    id("org.jetbrains.dokka") version "1.6.0"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
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

java {
    withSourcesJar()
}

val docsJar = tasks.register<Jar>("docsJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaJavadoc"))
}

artifacts {
    archives(docsJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components["kotlin"])
            artifacts {
                artifact(tasks.named("sourcesJar"))
                artifact(docsJar)
            }
            pom {
                name.set("kiosab")
                description.set("Kotlin IO Stream Async Bridge")
                url.set("https://github.com/orange-buffalo/kiosab")
                packaging = "jar"
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("orange-buffalo")
                        name.set("Bogdan Ilchyshyn")
                        email.set("orange-buffalo@users.noreply.github.com")
                    }
                }
                scm {
                    connection.set("scm:git@github.com:orange-buffalo/kiosab.git")
                    developerConnection.set("scm:git@github.com:orange-buffalo/kiosab.git")
                    url.set("https://github.com/orange-buffalo/kiosab")
                }
            }
        }
    }
}

signing {
    val ossrhSigningKey: String? by project
    val ossrhSigningPassword: String? by project
    useInMemoryPgpKeys(ossrhSigningKey, ossrhSigningPassword)
    sign(publishing.publications["mavenKotlin"])
}

nexusPublishing {
    repositories {
        create("OSSRH") {
            val ossrhUser: String? by project
            val ossrhPassword: String? by project
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/staging"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots"))
            username.set(ossrhUser)
            password.set(ossrhPassword)
        }
    }
}
