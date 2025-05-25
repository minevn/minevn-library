import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("io.github.goooler.shadow") version "8.1.7"
    id("maven-publish")
}

repositories {
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    implementation(project(":minevnlib-master"))
    implementation(project(":minevnlib-bukkit"))
    implementation(project(":minevnlib-bungee"))
    implementation("net.wesjd:anvilgui:1.10.5-SNAPSHOT")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
    from(project(":minevnlib-master").sourceSets.main.get().allSource)
    from(project(":minevnlib-bukkit").sourceSets.main.get().allSource)
    from(project(":minevnlib-bungee").sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.shadowJar.get().archiveFile)
            artifact(sourcesJar.get())
        }
    }
    repositories {
        maven {
            val mavenPath = project.properties["mavenPath"]
            url = if (mavenPath != null) {
                println("install to: $mavenPath")
                uri(mavenPath)
            } else {
                uri("${System.getProperty("user.home")}/.m2/repository")
            }
        }
    }
}

configurations {
    create("shadowNoKotlin") {
        extendsFrom(configurations.getByName("runtimeClasspath"))
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    }
}

tasks {
    val jarName = "MineVNLib"

    fun ShadowJar.changePackages() {
        // relocate to avoid conflict
        relocate("com.zaxxer.hikari", "net.minevn.libs.hikari")
        relocate("com.fasterxml.jackson", "net.minevn.libs.jackson")
        relocate("com.google.gson", "net.minevn.libs.gson")
        relocate("net.wesjd.anvilgui", "net.minevn.libs.anvilgui")
        relocate("com.cryptomorin.xseries", "net.minevn.libs.xseries")
        relocate("com.cronutils", "net.minevn.libs.cronutils")
        exclude("META-INF/versions/21/org/h2/util/Utils21.class")
        exclude("META-INF/*.RSA", "META-INF/*.DSA", "META-INF/*.SF")
    }

    // shadow with kotlin
    shadowJar {
        changePackages()
        archiveFileName.set("$jarName.jar")
    }

    // shadow without kotlin
    val noKotlinConfig = configurations.getByName("shadowNoKotlin")
    val shadowNoKotlin by creating(ShadowJar::class) {
        configurations = listOf(noKotlinConfig)
        changePackages()
        archiveFileName.set("$jarName-no-kotlin.jar")
    }

    val customCopy by creating(Task::class) {
        dependsOn(shadowJar, shadowNoKotlin)

        val path = project.properties["shadowPath"]
        if (path != null) {
            doLast {
                println(path)
                copy {
                    from("build/libs/$jarName.jar")
                    into(path)
                }
                copy {
                    from("build/libs/$jarName-no-kotlin.jar")
                    into(path)
                }
                println("Copied")
            }
        }
    }

    assemble {
        dependsOn(shadowJar, shadowNoKotlin, customCopy)
    }
}
