plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
}

dependencies {
    implementation(project(":minevnlib-master"))
    implementation(project(":minevnlib-bukkit"))
    implementation(project(":minevnlib-bungee"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.shadowJar.get().archiveFile)
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

tasks {
    val jarName = "MineVNLib"

    register("customCopy") {
        dependsOn(shadowJar)

        val path = project.properties["shadowPath"]
        if (path != null) {
            doLast {
                println(path)
                copy {
                    from("build/libs/$jarName.jar")
                    into(path)
                }
                println("Copied")
            }
        }
    }

    shadowJar {
        // relocate to avoid conflict
        relocate("com.zaxxer.hikari", "net.minevn.libs.hikari")
        relocate("com.fasterxml.jackson", "net.minevn.libs.jackson")
        relocate("com.google.gson", "net.minevn.libs.gson")
        exclude("META-INF/versions/21/org/h2/util/Utils21.class")
        archiveFileName.set("$jarName.jar")
    }

    assemble {
        dependsOn(shadowJar, get("customCopy"))
    }
}
