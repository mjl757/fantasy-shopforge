/**
 * Convention plugin for Kotlin Multiplatform library modules (:domain, :data).
 *
 * Configures:
 * - KMP with JVM and Android targets (V1)
 * - Structured for future iOS/Web target additions
 * - Android KMP library defaults (minSdk 26, compileSdk 36, JVM 17)
 *
 * Note: Uses com.android.kotlin.multiplatform.library (AGP 9.x KMP-first plugin)
 * instead of the legacy com.android.library + KMP combination which is no longer
 * supported in AGP 9.0+.
 *
 * Modules using this convention plugin must also declare:
 *   plugins {
 *       id("shopforge.kmp-library")
 *   }
 * and declare a namespace in the android { } block.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    // V1: JVM target for desktop unit tests
    jvm()

    androidLibrary {
        compileSdk = 36
        minSdk = 30
    }

    // Future targets (uncomment when adding iOS/Web support):
    // iosArm64()
    // iosSimulatorArm64()
    // iosX64()
    // js(IR) { browser(); nodejs() }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
