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
        versionCode = 5
        versionName = "5.0-clean"
        ndk { abiFilters += listOf("arm64-v8a") }
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
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.webkit:webkit:1.6.1")
}
