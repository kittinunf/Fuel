plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(Dependencies.forge)
    testCompile(Dependencies.mockServer)
}
