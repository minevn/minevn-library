import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.GradleException
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.ow2.asm:asm:9.9.1")
    }
}

plugins {
    id("com.gradleup.shadow") version "9.4.1"
    id("maven-publish")
}

repositories {
}

dependencies {
    implementation(project(":minevnlib-master"))
    implementation(project(":minevnlib-bukkit"))
    implementation(project(":minevnlib-bungee"))
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
    register("shadowNoKotlin") {
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

        doLast {
            rewriteClassVersion(
                archiveFile.get().asFile,
                setOf(
                    "net/minevn/libs/anvilgui/version/Wrapper26_R1.class",
                    $$"net/minevn/libs/anvilgui/version/Wrapper26_R1$AnvilContainer.class"
                ),
                Opcodes.V17
            )
        }
    }

    // shadow with kotlin
    shadowJar {
        changePackages()
        archiveFileName.set("$jarName.jar")
    }

    // shadow without kotlin
    val noKotlinConfig = configurations.getByName("shadowNoKotlin")
    val shadowNoKotlin by registering(ShadowJar::class) {
        configurations = listOf(noKotlinConfig)
        changePackages()
        archiveFileName.set("$jarName-no-kotlin.jar")
    }

    val customCopy by registering(Task::class) {
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

fun rewriteClassVersion(jarFile: File, classEntries: Set<String>, targetVersion: Int) {
    val tempFile = File.createTempFile(jarFile.nameWithoutExtension, ".jar", jarFile.parentFile)
    val rewrittenEntries = mutableSetOf<String>()
    try {
        ZipFile(jarFile).use { zipFile ->
            ZipOutputStream(tempFile.outputStream().buffered()).use { zipOutput ->
                val entries = zipFile.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val newEntry = ZipEntry(entry.name)
                    zipOutput.putNextEntry(newEntry)

                    if (!entry.isDirectory) {
                        val bytes = zipFile.getInputStream(entry).use { input ->
                            input.readBytes()
                        }

                        if (entry.name in classEntries) {
                            val classReader = ClassReader(bytes)
                            val classWriter = ClassWriter(0)
                            val classVisitor = object : ClassVisitor(Opcodes.ASM9, classWriter) {
                                override fun visit(
                                    version: Int,
                                    access: Int,
                                    name: String?,
                                    signature: String?,
                                    superName: String?,
                                    interfaces: Array<out String>?
                                ) {
                                    super.visit(targetVersion, access, name, signature, superName, interfaces)
                                }
                            }

                            classReader.accept(classVisitor, 0)
                            zipOutput.write(classWriter.toByteArray())
                            rewrittenEntries.add(entry.name)
                        } else {
                            zipOutput.write(bytes)
                        }
                    }

                    zipOutput.closeEntry()
                }
            }
        }

        val missingEntries = classEntries - rewrittenEntries
        if (missingEntries.isNotEmpty()) {
            throw GradleException(
                "Failed to rewrite class version in ${jarFile.name}. Missing entries: ${missingEntries.joinToString()}"
            )
        }

        tempFile.copyTo(jarFile, overwrite = true)
    } finally {
        tempFile.delete()
    }
}
