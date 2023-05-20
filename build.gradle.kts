import java.net.URI

plugins {
    id("org.springframework.boot") version "3.0.6"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    kotlin("plugin.allopen") version "1.7.22"
    id("maven-publish")
    id("signing")
}

group = "io.github.florentine-doemges"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "m2-dv8tion"
        url = URI("https://m2.dv8tion.net/releases")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.camel.springboot:camel-spring-boot-starter:4.0.0-M3")
    implementation("net.dv8tion:JDA:4.4.0_350")
    implementation("org.apache.camel:camel-core:4.0.0-M3")
    implementation("org.apache.camel.springboot:camel-spring-boot-starter:4.0.0-M3")
    implementation("org.apache.camel:camel-kotlin-dsl:4.0.0-M3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.6.4")
    testImplementation("com.appmattus.fixture:fixture:1.2.0")
    testImplementation("com.appmattus.fixture:fixture-javafaker:1.2.0")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("com.willowtreeapps.assertk:assertk:0.25")
    testImplementation("org.awaitility:awaitility:4.2.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named("bootJar") {
    enabled = false
}

tasks.named("jar"){
    enabled = true
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Camel Discord")
                description.set("A library providing an adapter from Camel to Discord using JDA")
                url.set("https://github.com/florentine-doemges/camel-discord")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("florentine-doemges")
                        name.set("Florentine Dömges")
                        email.set("florentine@doemges.net")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/florentine-doemges/camel-discord.git")
                    developerConnection.set("scm:git:ssh://github.com/florentine-doemges/camel-discord.git")
                    url.set("http://github.com/florentine-doemges/camel-discord")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
