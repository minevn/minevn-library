repositories {
    maven {
        setUrl("http://pack.minevn.net/repo/")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    compileOnly("minevn.depend:paper:1.12.2-b1619-no-gson")

    // database
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:9.22.0")
    implementation("com.h2database:h2:2.1.214")

    // JUnit
    testImplementation("minevn.depend:paper:1.12.2-b1619-no-gson")
}