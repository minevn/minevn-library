repositories {
    maven("https://repo.aikar.co/nexus/content/groups/aikar/")
}

dependencies {
    compileOnly(project(":minevnlib-master"))
    compileOnly("net.md-5:bungeecord-api:1.12-SNAPSHOT")
}