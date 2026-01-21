plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.myapp.tadu"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.myapp.tadu"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // ----------------------
    // Firebase (using BoM for version management)
    // ----------------------
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // ----------------------
    // Coroutines for Firebase
    // ----------------------
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation("androidx.compose.runtime:runtime-livedata")

    // ----------------------
    // Jetpack Compose
    // ----------------------
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material:material:1.6.7")  // Material 2
    implementation("androidx.compose.material3:material3")  // Material 3
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.animation:animation-core")
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")

    // ----------------------
    // Room
    // ----------------------
    implementation("androidx.room:room-runtime:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")
    kapt("androidx.room:room-compiler:2.7.1")

    // ----------------------
    // AndroidX / Core
    // ----------------------
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.navigation:navigation-runtime-android:2.9.0")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // ----------------------
    // Animation
    // ----------------------
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // ----------------------
    // Testing
    // ----------------------
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.04.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // ----------------------
    // Debug
    // ----------------------
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}