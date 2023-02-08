@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "7.4.0" apply false
        id("com.android.library") version "7.4.0" apply false
        id("org.jetbrains.kotlin.android") version "1.8.0" apply false
        id("com.google.devtools.ksp") version "1.8.0-1.0.9" apply false
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
