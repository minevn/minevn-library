import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

plugins {
    `java-library`
    kotlin("jvm") version "1.9.22"
}

allprojects {
    group = "net.minevn"
    version = "1.0.1"

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        // database
        implementation("com.zaxxer:HikariCP:5.1.0")
        implementation("com.h2database:h2:2.1.214")
        implementation("org.mariadb.jdbc:mariadb-java-client:2.7.11") { exclude("*") }

        // gson
        implementation("com.google.code.gson:gson:2.10.1")

        // JUnit
        testImplementation(platform("org.junit:junit-bom:5.9.2"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("io.mockk:mockk:1.13.7")
    }

    tasks {
        test {
            useJUnitPlatform()
        }

        register("printVersion") {
            doLast {
                println("${project.version}")
            }
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
