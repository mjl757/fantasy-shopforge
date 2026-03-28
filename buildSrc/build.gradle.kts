plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // These are needed for precompiled convention plugins and submodules to reference plugin IDs.
    // Pin versions to match libs.versions.toml.
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.1.10")
    implementation("com.android.tools.build:gradle:9.1.0")
}
