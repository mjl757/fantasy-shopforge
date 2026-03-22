plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    // V1 targets: JVM (for unit tests) and Android
    jvm()

    android {
        namespace = "dev.lievens.shopforge.domain"
        compileSdk = 36
        minSdk = 26
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // Structured for future iOS/Web targets:
    // iosArm64()
    // iosSimulatorArm64()
    // js(IR) { browser(); nodejs() }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
