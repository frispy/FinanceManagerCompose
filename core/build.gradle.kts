plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

group = "core.financemanager"
version = "1.0-SNAPSHOT"

kotlin {
    // Keep this consistent with your desktop app
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                // business logic & data
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)

                // database
                implementation(libs.room.runtime)
                implementation(libs.sqlite.bundled)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }

    jvmToolchain(21)
}

// room compiler configuration for the core module
dependencies {
    add("kspJvm", libs.room.compiler)
}