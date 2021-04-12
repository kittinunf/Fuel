dependencies {
    api(Result.dependency)

    testImplementation(project(Fuel.Test.name))
    testImplementation(Json.dependency)
}

tasks.getByName("test") {
    inputs.dir("src/test/assets")
}
