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
        versionCode = 3
        versionName = "3.0-premium-bass"
        ndk { abiFilters += listOf("arm64-v8a") }
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17 -O2 -frtti -fexceptions"
                arguments += listOf("-DANDROID_STL=c++_shared", "-DOBOE_DIR=oboe")
            }
        }
    }
    buildTypes { release { isMinifyEnabled = false } }
    externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt") } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.webkit:webkit:1.6.1")
}
