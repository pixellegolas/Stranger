plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.strangerpro"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.strangerpro.final"
        minSdk = 28
        targetSdk = 34
        versionCode = 6
        versionName = "6.0-agp852-fix"
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17 -O2 -frtti -fexceptions"
                arguments += listOf("-DANDROID_STL=c++_shared")
            }
        }
    }
    buildTypes {
        release { isMinifyEnabled = false }
        debug { isMinifyEnabled = false }
    }
    externalNativeBuild {
        cmake { path = file("src/main/cpp/CMakeLists.txt"); version = "3.22.1" }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // FIX: Only core-ktx, no webkit to avoid ConstraintHandler alignWith crash in AGP 8.2.2
    // androidx.webkit caused Cannot mutate dependencies after configuration was resolved
    implementation("androidx.core:core-ktx:1.10.1")
}
