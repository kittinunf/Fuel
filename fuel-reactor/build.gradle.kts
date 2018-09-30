repositories {
    maven(url = "http://repo.spring.io/milestone")
}

dependencies {
    compile(project(":fuel"))
    compile(Dependencies.reactorCore)
    testCompile(Dependencies.reactorTest)
    testCompile(Dependencies.mockServer)
    testCompile(project(":fuel-jackson"))
}
