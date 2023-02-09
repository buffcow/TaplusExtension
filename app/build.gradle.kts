@file:Suppress("UnstableApiUsage")

import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.android")
}

val prop by lazy {
    Properties().apply {
        load(File(rootDir, "local.properties").inputStream())
    }
}

android {
    compileSdk = 33
    namespace = "io.github.yangyiyu08.taplusext"

    defaultConfig {
        minSdk = 24
        targetSdk = 33
        versionCode = 7
        versionName = "1.1.4"
        applicationId = android.namespace
    }

    signingConfigs {
        create("release") {
            enableV3Signing = true
            storeFile = file(prop.getProperty("sign.storeFile"))
            keyAlias = prop.getProperty("sign.keyAlias")
            keyPassword = prop.getProperty("sign.storePassword")
            storePassword = prop.getProperty("sign.storePassword")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
    ksp("com.highcapable.yukihookapi:ksp-xposed:1.1.8")
    implementation("androidx.annotation:annotation:1.5.0")
    implementation("com.highcapable.yukihookapi:api:1.1.8")
}
