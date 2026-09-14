plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nowbrief.livecapsule"

    // Exclusive Android 16 targeting, as required.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nowbrief.livecapsule"
        minSdk = 33          // Live Updates degrade gracefully below API 36 (see NowBriefNotifier)
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Signs the release build with AGP's auto-generated debug keystore so the
            // resulting APK is a valid, installable, signed package for side-loading on
            // your own device via the GitHub Actions artifact. Before ever publishing
            // this app anywhere public (Play Store, F-Droid, etc.), replace this with a
            // real release signingConfig backed by your own keystore — a debug-signed
            // APK must never be distributed publicly.
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    // core-remoteviews / androidx.core 1.15+ carries the setRequestPromotedOngoing() shim.
    implementation("com.google.android.material:material:1.12.0")
}
