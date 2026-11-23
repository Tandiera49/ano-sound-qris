plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.tandiera49.anosoundqris"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tandiera49.anosoundqris"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}
