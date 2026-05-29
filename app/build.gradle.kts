plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.morphiclabs.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.morphiclabs.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // Updated to Java 17 for consistency with other modules
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // Updated for compatibility with Kotlin 1.9.24
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Module dependencies
    implementation(project(":ui"))
    implementation(project(":di")) // Even if empty, include it for the structure
    implementation(project(":data")) // MainActivity needs access to MiddlewareLocal directly for now
    implementation(project(":core-base"))
    implementation(project(":core"))

    // General Android dependencies needed by the app module itself
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1") // Align with module UI
    implementation("androidx.activity:activity-compose:1.8.1") // Updated to 1.8.1

    // Base Compose dependencies
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")

    // Remove UI specific dependencies, as they are now in the :ui module
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    // implementation("androidx.compose.ui:ui") // Moved to :ui
    // implementation("androidx.compose.ui:ui-graphics") // Moved to :ui
    // implementation("androidx.compose.ui:ui-tooling-preview") // Moved to :ui
    // implementation("androidx.compose.material3:material3") // Moved to :ui

    implementation("com.squareup.okhttp3:okhttp:4.12.0") // Keep if needed for app module directly

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // Remove UI specific test dependencies, as they are now in the :ui module
    // androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00")) // Moved to :ui
    // androidTestImplementation("androidx.compose.ui:ui-test-junit4") // Moved to :ui
    // debugImplementation("androidx.compose.ui:ui-tooling") // Moved to :ui
    // debugImplementation("androidx.compose.ui:ui-test-manifest") // Moved to :ui
}
