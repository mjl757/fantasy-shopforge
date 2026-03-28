pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // Resolve plugin versions for precompiled script plugins in buildSrc
    resolutionStrategy {
        eachPlugin {
            when (requested.id.namespace) {
                "com.android" -> useVersion("9.1.0")
                "org.jetbrains.kotlin" -> useVersion("2.3.20")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "buildSrc"
