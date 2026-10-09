plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.strangerpro"
    compileSdk = 34
    //...
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {
    // This still fixes your first error: Unresolved reference: activity
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.activity:activity-compose:1.9.0")
}
