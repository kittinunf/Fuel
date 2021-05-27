dependencies {
    api(project(Fuel.name))

    implementation(Androidx.Arch.extensions)

    testImplementation(project(Fuel.Test.name))
    testImplementation(Android.Arch.testingCore)
    testImplementation(Androidx.Test.junit)
    testImplementation(RoboElectric.dependency)
}
