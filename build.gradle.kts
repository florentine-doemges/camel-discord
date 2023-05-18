plugins {
    kotlin("jvm") version "1.8.20"
    id("maven-publish")
    id("signing")
}

group = "io.github.florentine-doemges"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Camel Discord")
                description.set("A library providing an adapter from Camel to Discord using JDA")
                url.set("https://your-library-url.com")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("yourId")
                        name.set("Your Name")
                        email.set("your-email@example.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/username/repo.git")
                    developerConnection.set("scm:git:ssh://github.com/username/repo.git")
                    url.set("http://github.com/username/repo")
                }
            }
        }
    }
}