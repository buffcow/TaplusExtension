@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        kotlin("android") version "1.8.20" apply false
        id("com.android.application") version "8.0.0" apply false
        id("com.android.library") version "8.0.0" apply false
        id("com.google.devtools.ksp") version "1.8.20-1.0.11" apply false
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://api.xposed.info/") }
    }
}

rootProject.name = "TaplusExtension"
include(":app")
