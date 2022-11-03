@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 33
    namespace = "com.qingyu.micsm"

    defaultConfig {
        minSdk = 24
        targetSdk = 33
        versionCode = 5
        versionName = "1.1.2"
        applicationId = android.namespace
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
    ksp("com.highcapable.yukihookapi:ksp-xposed:1.1.4")
    implementation("androidx.annotation:annotation:1.5.0")
    implementation("com.highcapable.yukihookapi:api:1.1.4")
}
