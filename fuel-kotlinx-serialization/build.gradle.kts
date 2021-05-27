plugins {
    kotlin("jvm") apply false
    id(Kotlinx.Serialization.plugin)
}

repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    api(project(Fuel.name))

    implementation(KotlinX.serialization.json)

    testImplementation(project(Fuel.Test.name))
}
