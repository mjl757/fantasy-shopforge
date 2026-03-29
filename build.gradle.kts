// Top-level build file.
// Plugin versions for AGP and Kotlin are managed through buildSrc/build.gradle.kts.
// Other plugins are declared here (apply false) so submodules can apply them without specifying a version.
plugins {
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
