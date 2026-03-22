plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.sqldelight)
}

kotlin {
    // V1 targets: JVM (for unit tests) and Android
    jvm()

    android {
        namespace = "com.shopforge.data"
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
            implementation(project(":domain"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.sqldelight.coroutines.extensions)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmTest.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.junit.jupiter)
        }
    }
}

sqldelight {
    databases {
        create("ShopForgeDatabase") {
            packageName.set("com.shopforge.data.db")
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
    }
}
