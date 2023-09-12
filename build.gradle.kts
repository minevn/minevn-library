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

    val compileKotlin: KotlinCompile by tasks
    compileKotlin.kotlinOptions {
        jvmTarget = "17"
    }
    val compileTestKotlin: KotlinCompile by tasks
    compileTestKotlin.kotlinOptions {
        jvmTarget = "17"
    }
}
