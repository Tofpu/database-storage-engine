plugins {
    java
    id("maven-publish")
}

group = "io.tofpu"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.xerial:sqlite-jdbc:3.36.0.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}