plugins {
    id(KotlinX.Serialization.plugin)
}

repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    api(project(Fuel.name))

    implementation(KotlinX.Serialization.Json.dependency)

    testImplementation(project(Fuel.Test.name))
}
