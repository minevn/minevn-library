repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.viaversion.com/everything")
}

dependencies {
    compileOnly(project(":minevnlib-master"))
    compileOnly("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT")

    // other libs
    implementation("com.github.cryptomorin:XSeries:13.7.1") { isTransitive = false }
    implementation("net.wesjd:anvilgui:1.10.13-SNAPSHOT")
    compileOnly("com.viaversion:viaversion-api:5.9.1")
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
