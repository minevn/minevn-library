import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm") version "1.8.21"
}

allprojects {
    group = "net.minevn"
    version = "1.0.0"

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        // libs
        implementation("org.danilopianini:khttp:1.4.0")

        // database
        implementation("com.zaxxer:HikariCP:5.0.1")
        implementation("org.flywaydb:flyway-core:9.22.0")
        implementation("com.h2database:h2:2.1.214")

        // JUnit
        testImplementation(platform("org.junit:junit-bom:5.9.2"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("io.mockk:mockk:1.13.7")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    val compileKotlin: KotlinCompile by tasks
    compileKotlin.kotlinOptions {
        jvmTarget = "17"
    }
    val compileTestKotlin: KotlinCompile by tasks
    compileTestKotlin.kotlinOptions {
        jvmTarget = "17"
    }

    configurations {
        testImplementation.get().extendsFrom(compileOnly.get())
    }
}
