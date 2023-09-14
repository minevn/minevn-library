repositories {
    maven {
        setUrl("http://pack.minevn.net/repo/")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    compileOnly("minevn.depend:paper:1.12.2-b1619")

    // JUnit
    testImplementation("minevn.depend:paper:1.12.2-b1619")
}