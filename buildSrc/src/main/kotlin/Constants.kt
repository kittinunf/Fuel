// Library version
object Fuel {
    const val publishVersion = "2.2.1"
    const val groupId = "com.github.kittinunf.fuel"

    const val compileSdkVersion = 28
    const val minSdkVersion = 19

    const val name = ":fuel"

    object Android {
        const val name = ":fuel-android"
    }

    object Coroutines {
        const val name = ":fuel-coroutines"
    }

    object Forge {
        const val name = ":fuel-forge"
    }

    object Gson {
        const val name = ":fuel-gson"
    }

    object Jackson {
        const val name = ":fuel-jackson"
    }

    object Json {
        const val name = ":fuel-json"
    }

    object KotlinSerialization {
        const val name = ":fuel-kotlinx-serialization"
    }

    object LiveData {
        const val name = ":fuel-livedata"
    }

    object Moshi {
        const val name = ":fuel-moshi"
    }

    object Reactor {
        const val name = ":fuel-reactor"
    }

    object RxJava {
        const val name = ":fuel-rxjava"
    }

    object Stetho {
        const val name = ":fuel-stetho"
    }

    object Test {
        const val name = ":fuel-test"
    }
}

// Core dependencies
object Kotlin {
    const val version = "1.3.61"
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
    const val plugin = "kotlin"
    const val androidPlugin = "kotlin-android"
    const val androidExtensionsPlugin = "kotlin-android-extensions"
}

object Result {
    const val version = "2.2.0"
    const val dependency = "com.github.kittinunf.result:result:$version"
}

object Json {
    const val version = "20180813"
    const val dependency = "org.json:json:$version"
}

object Android {
    const val version = "3.4.0"
    const val appPlugin = "com.android.application"
    const val libPlugin = "com.android.library"

    object Arch {
        const val version = "1.1.1"
        const val testingCore = "android.arch.core:core-testing:$version"
    }
}

object AndroidX {
    val annotation = "androidx.annotation:annotation:1.0.0"
    val appCompat = "androidx.appcompat:appcompat:1.0.2"

    object Arch {
        const val version = "2.0.0"
        const val extensions = "androidx.lifecycle:lifecycle-extensions:$version"
    }

    object Espresso {
        const val version = "3.1.0"
        const val core = "androidx.test.espresso:espresso-core:$version"
        const val intents = "androidx.test.espresso:espresso-intents:$version"
    }

    // Testing dependencies
    object Test {
        const val rulesVersion = "1.1.0"
        const val junitVersion = "1.0.0"
        const val rules = "androidx.test:rules:$rulesVersion"
        const val junit = "androidx.test.ext:junit:$junitVersion"
    }
}

// Modules dependencies
object Forge {
    const val version = "1.0.0-alpha2"
    const val dependency = "com.github.kittinunf.forge:forge:$version"
}

object Gson {
    const val version = "2.8.5"
    const val dependency = "com.google.code.gson:gson:$version"
}

object Jackson {
    const val version = "2.9.9"
    const val dependency = "com.fasterxml.jackson.module:jackson-module-kotlin:$version"
}

object KotlinX {
    object Coroutines {
        const val version = "1.3.3"
        val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        val jvm = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
    }

    object Serialization {
        const val version = "0.14.0"
        const val dependency = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:$version"
        const val plugin = "kotlinx-serialization"
    }
}

object Moshi {
    const val version = "1.8.0"
    const val dependency = "com.squareup.moshi:moshi:$version"
}

object Reactor {
    const val version = "3.2.8.RELEASE"
    const val core = "io.projectreactor:reactor-core:$version"
    const val test = "io.projectreactor:reactor-test:$version"
}

object RxJava {
    object Jvm {
        const val version = "2.2.8"
        const val dependency = "io.reactivex.rxjava2:rxjava:$version"
    }

    object Android {
        const val version = "2.1.0"
        const val dependency = "io.reactivex.rxjava2:rxandroid:$version"
    }
}

// Lint
object Ktlint {
    const val version = "1.24.0"
    const val plugin = "org.jmailen.kotlinter"
}

// Testing dependencies
object JUnit {
    const val version = "4.12"
    const val dependency = "junit:junit:$version"
}

object MockServer {
    const val version = "5.4.1"
    const val dependency = "org.mock-server:mockserver-netty:$version"
}

object Jacoco {
    const val version = "0.8.4"
    const val plugin = "jacoco"

    object Android {
        const val version = "0.1.4"
        const val plugin = "jacoco-android"
    }
}

object RoboElectric {
    const val version = "4.2.1"
    const val dependency = "org.robolectric:robolectric:$version"
}

object Release {
    object MavenPublish {
        const val plugin = "maven-publish"
    }

    object Bintray {
        const val version = "1.8.4"
        const val plugin = "com.jfrog.bintray"
    }
}

object Stetho {
    const val version = "1.5.1"
    const val dependency = "com.facebook.stetho:stetho:$version"

    object StethoUrlConnection {
        const val dependency = "com.facebook.stetho:stetho-urlconnection:$version"
    }
}
