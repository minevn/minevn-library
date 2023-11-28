repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly(project(":minevnlib-master"))
    compileOnly("net.md-5:bungeecord-api:1.12-SNAPSHOT")
}