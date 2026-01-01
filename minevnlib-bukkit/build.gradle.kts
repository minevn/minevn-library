repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(project(":minevnlib-master"))
    compileOnly("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT")

    // other libs
    implementation("com.github.cryptomorin:XSeries:13.6.0") { isTransitive = false }
    implementation("net.wesjd:anvilgui:1.10.11-SNAPSHOT")
}

tasks.processResources {
    outputs.upToDateWhen { false }
    filesMatching(listOf("**/plugin.yml")) {
        expand(mapOf("version" to project.version.toString()))
        println("$name: set version to ${project.version}")
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    filteringCharset = Charsets.UTF_8.name()
}
