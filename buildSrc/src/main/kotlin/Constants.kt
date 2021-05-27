// Library version
object Fuel {
    const val publishVersion = "2.3.1"
    const val groupId = "com.github.kittinunf.fuel"

    const val compileSdkVersion = 29
    const val minSdkVersion = 19

    const val name = ":fuel"

    object Package {

        const val repo = "maven"
        const val name = "Fuel-Android"
        const val desc = "The easiest HTTP networking library in Kotlin/Android"
        const val userOrg = "kittinunf"
        const val url = "https://github.com/kittinunf/Fuel"
        const val scm = "git@github.com:kittinunf/fuel.git"
        const val licenseName = "MIT License"
        const val licenseUrl = "http://www.opensource.org/licenses/mit-license.php"
    }

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

object Result {
    const val dependency = "com.github.kittinunf.result:result:_"
}

object Json {
    const val dependency = "org.json:json:_"
}

object Android {
    const val appPlugin = "com.android.application"
    const val libPlugin = "com.android.library"

    object Arch {
        const val testingCore = "android.arch.core:core-testing:_"
    }
}

object Androidx {
    val annotation = "androidx.annotation:annotation:_"
    val appCompat = "androidx.appcompat:appcompat:_"

    object Arch {
        const val extensions = "androidx.lifecycle:lifecycle-extensions:_"
    }

    object Espresso {
        const val core = "androidx.test.espresso:espresso-core:_"
        const val intents = "androidx.test.espresso:espresso-intents:_"
    }

    // Testing dependencies
    object Test {
        const val rules = "androidx.test:rules:_"
        const val junit = "androidx.test.ext:junit:_"
    }
}

// Modules dependencies
object Forge {
    const val dependency = "com.github.kittinunf.forge:forge:_"
}

object Gson {
    const val dependency = "com.google.code.gson:gson:_"
}

object Jackson {
    const val dependency = "com.fasterxml.jackson.module:jackson-module-kotlin:_"
}

object Kotlinx {
    object Coroutines {
        val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:_"
        val jvm = "org.jetbrains.kotlinx:kotlinx-coroutines-core:_"
    }

    object Serialization {
        const val plugin = "kotlinx-serialization"

        const val dependency = "org.jetbrains.kotlinx:kotlinx-serialization-core:_"

        object Json {
            const val dependency = "org.jetbrains.kotlinx:kotlinx-serialization-json:_"
        }
    }
}

object Moshi {
    const val dependency = "com.squareup.moshi:moshi:_"

    const val codegen = "com.squareup.moshi:moshi-kotlin-codegen:_"
}

object Reactor {
    const val core = "io.projectreactor:reactor-core:_"
    const val test = "io.projectreactor:reactor-test:_"
}

object RxJava {
    object Jvm {
        const val dependency = "io.reactivex.rxjava2:rxjava:_"
    }

    object Android {
        const val dependency = "io.reactivex.rxjava2:rxandroid:_"
    }
}

// Lint
object Ktlint {
    const val plugin = "org.jmailen.kotlinter"
}

// Testing dependencies
object JUnit {
    const val dependency = "junit:junit:_"
}

object MockServer {
    const val dependency = "org.mock-server:mockserver-netty:_"
}

object Jacoco {
    const val plugin = "jacoco"

    object Android {
        const val plugin = "jacoco-android"
    }
}

object RoboElectric {
    const val dependency = "org.robolectric:robolectric:_"
}

object Release {
    object MavenPublish {
        const val plugin = "maven-publish"
    }

    object Bintray {
        const val plugin = "com.jfrog.bintray"
    }
}

object Stetho {
    const val dependency = "com.facebook.stetho:stetho:_"

    object StethoUrlConnection {
        const val dependency = "com.facebook.stetho:stetho-urlconnection:_"
    }
}
