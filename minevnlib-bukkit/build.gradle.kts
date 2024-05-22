repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(project(":minevnlib-master"))
    compileOnly("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT")
    implementation("com.github.cryptomorin:XSeries:10.0.0") { isTransitive = false }
}
