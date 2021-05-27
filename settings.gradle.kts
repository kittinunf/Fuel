pluginManagement {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://plugins.gradle.org/m2/")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.library") {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
            if (requested.id.id == "jacoco-android") {
                useModule("com.dicedmelon.gradle:jacoco-android:${requested.version}")
            }
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}

plugins {
    // See https://jmfayard.github.io/refreshVersions
    id("de.fayard.refreshVersions") version "0.10.0"
}

val projects = listOf(
    ":fuel",
    ":fuel-android",
    ":fuel-coroutines",
    ":fuel-forge",
    ":fuel-gson",
    ":fuel-jackson",
    ":fuel-json",
    ":fuel-kotlinx-serialization",
    ":fuel-livedata",
    ":fuel-moshi",
    ":fuel-reactor",
    ":fuel-rxjava",
    ":fuel-stetho",
    ":fuel-test"
)

include(*(projects.toTypedArray()))

val includeSample: String by settings
if (includeSample == "true") {
    include(":sample")
}
