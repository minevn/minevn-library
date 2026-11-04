repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.viaversion.com/everything")
}

dependencies {
    compileOnly(project(":minevnlib-master"))
    compileOnly("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT")

    // other libs
    implementation("com.github.cryptomorin:XSeries:14.0.0") { isTransitive = false }
    implementation("net.wesjd:anvilgui:1.10.13-SNAPSHOT")
    // dialog qua ViaVersion: api có Holder, Types, nbt, mcstructs; common có packet type + protocol 1.21.6
    compileOnly("com.viaversion:viaversion-api:5.11.0")
    compileOnly("com.viaversion:viaversion-common:5.11.0") { isTransitive = false }
    // Component của model dialog, server Paper đã có sẵn
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.17.0")
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
