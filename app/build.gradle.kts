plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.25" // FIX 2: was 1.9.22, 1.5.15 needs 1.9.25
}

android {
    namespace = "com.strangerpro"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.strangerpro"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // FIX 1: this was missing - causes Unresolved reference: activity
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.activity:activity-compose:1.9.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
}
