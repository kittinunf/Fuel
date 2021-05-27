import com.android.build.gradle.BaseExtension
import com.jfrog.bintray.gradle.BintrayExtension.GpgConfig
import de.fayard.refreshVersions.core.versionFor
import org.gradle.api.publish.maven.MavenPom
import org.jmailen.gradle.kotlinter.KotlinterExtension
import org.jmailen.gradle.kotlinter.support.ReporterType
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    java
    kotlin("jvm") apply false
    id(Android.libPlugin)  apply false
    //id(Jacoco.Android.plugin) apply false
    id("org.jetbrains.kotlin.plugin.serialization") apply false
    id(Ktlint.plugin) apply false

    `maven-publish`
    id(Release.Bintray.plugin)
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        jcenter()
    }
}

val androidModules = listOf("fuel-android", "fuel-livedata", "fuel-stetho")
val androidSampleModules = listOf("sample")
val testModules = listOf("fuel-test")

subprojects {
    val isAndroidModule = project.name in androidModules
    val isSample = project.name in androidSampleModules
    val isTest = project.name in testModules
    val isJvmModule = !isAndroidModule && !isSample

    if (isJvmModule) {
        apply {
            plugin("org.jetbrains.kotlin.jvm")
            plugin(Jacoco.plugin)
        }

        configure<JacocoPluginExtension> {
            toolVersion = versionFor("version.jacoco")
        }

        configure<JavaPluginConvention> {
            sourceCompatibility = JavaVersion.VERSION_1_7
            targetCompatibility = JavaVersion.VERSION_1_7

            sourceSets {
                getByName("main").java.srcDirs("src/main/kotlin")
                getByName("test").java.srcDirs("src/main/kotlin")
            }
        }

        tasks.withType<JacocoReport> {
            reports {
                html.isEnabled = false
                xml.isEnabled = true
                csv.isEnabled = false
            }
        }

        val sourcesJar by tasks.registering(Jar::class) {
            from(sourceSets["main"].allSource)
            archiveClassifier.set("sources")
        }

        val doc by tasks.creating(Javadoc::class) {
            isFailOnError = false
            source = sourceSets["main"].allJava
        }
    }

    if (isAndroidModule) {
        apply {
            plugin(Android.libPlugin)
            plugin("org.jetbrains.kotlin.android")
            plugin("org.jetbrains.kotlin.android.extensions")
            //plugin(Jacoco.Android.plugin)
        }

        configure<BaseExtension> {
            compileSdkVersion(Fuel.compileSdkVersion)

            defaultConfig {
                minSdkVersion(Fuel.minSdkVersion)
                targetSdkVersion(Fuel.compileSdkVersion)
                versionCode = 1
                versionName = Fuel.publishVersion
            }

            sourceSets {
                getByName("main").java.srcDirs("src/main/kotlin")
                getByName("test").java.srcDirs("src/test/kotlin")
            }

            compileOptions {
                setSourceCompatibility(JavaVersion.VERSION_1_7)
                setTargetCompatibility(JavaVersion.VERSION_1_7)
            }

            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                    consumerProguardFiles("proguard-rules.pro")
                }
            }

            lintOptions {
                isAbortOnError = false
            }

            testOptions {
                unitTests.isReturnDefaultValues = true
            }

            val sourcesJar by tasks.registering(Jar::class) {
                from(sourceSets["main"].java.srcDirs)
                archiveClassifier.set("sources")
            }

            val doc by tasks.creating(Javadoc::class) {
                isFailOnError = false
                source = sourceSets["main"].java.sourceFiles
                classpath += files(bootClasspath.joinToString(File.pathSeparator))
                classpath += configurations.compile
            }
        }

        /**
        configure<JacocoAndroidUnitTestReportExtension> {
            csv.enabled(false)
            html.enabled(true)
            xml.enabled(true)
        }
        **/
    }

    if (!isSample) {
        apply {
            plugin(Release.MavenPublish.plugin)
            plugin(Release.Bintray.plugin)
            plugin(Ktlint.plugin)
        }

        configure<KotlinterExtension> {
            reporters = arrayOf(ReporterType.plain.name, ReporterType.checkstyle.name)
        }

        tasks.named<LintTask>("lintKotlinMain") {
            enabled = false
        }

        tasks.named<LintTask>("lintKotlinTest") {
            enabled = false
        }

        tasks.withType<Test> {
            maxHeapSize = "4g"
        }

        dependencies {
            implementation(Kotlin.stdlib.jdk7)

            testImplementation(JUnit.dependency)
        }

        if (!isTest) {
            version = Fuel.publishVersion
            group = Fuel.groupId
            bintray {
                user = findProperty("BINTRAY_USER") as? String
                key = findProperty("BINTRAY_KEY") as? String
                setPublications(project.name)
                with(pkg) {
                    repo = Fuel.Package.repo
                    name = Fuel.Package.name
                    desc = Fuel.Package.desc
                    userOrg = Fuel.Package.userOrg
                    websiteUrl = Fuel.Package.url
                    vcsUrl = Fuel.Package.url
                    setLicenses(Fuel.Package.licenseName)
                    with(version) {
                        name = Fuel.publishVersion
                        gpg(delegateClosureOf<GpgConfig> {
                            sign = true
                            passphrase = System.getenv("GPG_PASSPHRASE") ?: ""
                        })
                    }
                }
            }

            fun MavenPom.addDependencies() = withXml {
                asNode().appendNode("dependencies").let { depNode ->
                    configurations.implementation.get().allDependencies.forEach {
                        depNode.appendNode("dependency").apply {
                            appendNode("groupId", it.group)
                            appendNode("artifactId", it.name)
                            appendNode("version", it.version)
                        }
                    }
                }
            }

            val javadocJar by tasks.creating(Jar::class) {
                val doc by tasks
                dependsOn(doc)
                from(doc)

                archiveClassifier.set("javadoc")
            }

            val sourcesJar by tasks
            publishing {
                publications {
                    register(project.name, MavenPublication::class) {
                        if (project.hasProperty("android")) {
                            artifact("$buildDir/outputs/aar/${project.name}-release.aar") {
                                builtBy(tasks.getByPath("assemble"))
                            }
                        } else {
                            from(components["java"])
                        }
                        artifact(sourcesJar)
                        artifact(javadocJar)
                        groupId = Fuel.groupId
                        artifactId = project.name
                        version = Fuel.publishVersion

                        pom {
                            name.set(project.name)
                            description.set(Fuel.Package.desc)

                            packaging = if (project.hasProperty("android")) "aar" else "jar"
                            url.set(Fuel.Package.url)

                            licenses {
                                license {
                                    name.set(Fuel.Package.licenseName)
                                    url.set(Fuel.Package.licenseUrl)
                                }
                            }

                            developers {
                                developer {
                                    name.set("kittinunf")
                                }

                                developer {
                                    name.set("iNoles")
                                }

                                developer {
                                    name.set("KucherenkoIhor")
                                }

                                developer {
                                    name.set("lucasqueiroz")
                                }

                                developer {
                                    name.set("lucasvalenteds")
                                }

                                developer {
                                    name.set("NikkyAI")
                                }

                                developer {
                                    name.set("SleeplessByte")
                                }
                            }

                            contributors {
                                // https://github.com/kittinunf/fuel/graphs/contributors
                                contributor {
                                    name.set("Danilo Pianini")
                                }
                            }
                            scm {
                                url.set(Fuel.Package.url)
                                connection.set(Fuel.Package.scm)
                                developerConnection.set(Fuel.Package.scm)
                            }
                        }

                        if (project.hasProperty("android")) {
                            pom.addDependencies()
                        }
                    }
                }
            }
        }
    }
}
