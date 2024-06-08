import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    kotlin("jvm") version "2.0.0"
}

allprojects {
    group = "net.minevn"
    version = "1.1.2"

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        // database
        implementation("com.zaxxer:HikariCP:4.0.3")
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

    java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    kotlin.compilerOptions.jvmTarget = JvmTarget.JVM_1_8

    configurations {
        testImplementation.get().extendsFrom(compileOnly.get())
    }
}
