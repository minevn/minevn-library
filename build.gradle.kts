plugins {
    `java-library`
    kotlin("jvm") version "2.3.21"
}

allprojects {
    group = "net.minevn"
    version = "26.1.1"

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        maven("https://mvn.wesjd.net/") // anvil gui
    }

    dependencies {
        // database
        implementation("com.zaxxer:HikariCP:7.0.2")
        implementation("com.h2database:h2:2.1.214")
        implementation("org.mariadb.jdbc:mariadb-java-client:3.5.9") { exclude("*") }

        // ohthers
        implementation(kotlin("reflect"))
        implementation("com.google.code.gson:gson:2.14.0")
        implementation("com.cronutils:cron-utils:9.2.1")
        implementation("at.favre.lib:bcrypt:0.10.2")

        // JUnit
        testImplementation(platform("org.junit:junit-bom:6.1.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("io.mockk:mockk:1.14.9")
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

    kotlin {
        jvmToolchain(17)
    }

    configurations {
        testImplementation.get().extendsFrom(compileOnly.get())
    }
}
