import com.android.build.gradle.BaseExtension

plugins {
    id(Android.appPlugin)
    kotlin("android")
    kotlin("android.extensions")
}

dependencies {
    // fuel related libraries
    implementation(project(Fuel.Android.name))
    implementation(project(Fuel.Coroutines.name))
    implementation(project(Fuel.Gson.name))
    implementation(project(Fuel.LiveData.name))
    implementation(project(Fuel.RxJava.name))
    implementation(project(Fuel.Stetho.name))

    // dependencies
    implementation(Androidx.appCompat)
    implementation(Gson.dependency)
    implementation(Kotlin.stdlib.common)
    implementation(Kotlinx.Coroutines.android)
    implementation(RxJava.Android.dependency)
    implementation(Stetho.StethoUrlConnection.dependency)
    implementation(Stetho.dependency)

    // test dependencies
    androidTestImplementation(Androidx.annotation)
    androidTestImplementation(Androidx.Test.junit)
    androidTestImplementation(Androidx.Test.rules)
    androidTestImplementation(Androidx.Espresso.core)
    androidTestImplementation(Androidx.Espresso.intents)
}

configure<BaseExtension> {
    compileSdkVersion(Fuel.compileSdkVersion)

    defaultConfig {
        applicationId = "com.example.fuel"
        minSdkVersion(Fuel.minSdkVersion)
        targetSdkVersion(Fuel.compileSdkVersion)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    packagingOptions {
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE.txt")
    }
}
