plugins {
    `java-library`
    `maven-publish`
    id("io.freefair.lombok") version "8.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("com.github.stefvanschie.inventoryframework:IF:0.10.13")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            project.shadow.component(this)
        }
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveVersion.set("1.0-SNAPSHOT")

        relocate("com.zaxxer", "it.renvins.region.hikari")
        relocate("com.github.stefvanschie.inventoryframework", "it.renvins.region.if")
    }

    assemble {
        dependsOn(shadowJar)
    }
}