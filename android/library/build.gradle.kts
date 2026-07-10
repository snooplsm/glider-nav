plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("maven-publish")
}

val isJitPackBuild = System.getenv("JITPACK") == "true"
val jitPackGroup = listOfNotNull(System.getenv("GROUP"), System.getenv("ARTIFACT"))
    .joinToString(".")
    .ifBlank { "com.github.snooplsm.glider-nav" }

group = if (isJitPackBuild) jitPackGroup else "one.adverse.glider"
version = if (isJitPackBuild) (System.getenv("VERSION") ?: "v0.1.3") else "0.1.3"

android {
    namespace = "one.adverse.glider"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("androidx.compose.foundation:foundation:1.9.0")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.ui:ui:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    debugImplementation("androidx.compose.ui:ui-tooling:1.9.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.1.10")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = "glider-nav"
            version = project.version.toString()

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
