@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "7.3.0" apply false
        id("com.android.library") version "7.3.0" apply false
        id("org.jetbrains.kotlin.android") version "1.7.20" apply false
        id("com.google.devtools.ksp") version "1.7.20-1.0.7" apply false
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

rootProject.name = "FuckContentExtension"
include(":app")
