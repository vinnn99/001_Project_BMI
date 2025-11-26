plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.17.0")

    // Jetpack Compose (using BOM for consistent versions)
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Firebase (use BOM to align versions)
    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Google Play services base for manifest resource
    implementation("com.google.android.gms:play-services-base:18.2.0")

    // Test Dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Read DEEPSEEK_API_KEY from project properties or environment (safe fallback to empty string)
val DEEPSEEK_API_KEY: String = (project.findProperty("DEEPSEEK_API_KEY") as String?)
    ?: System.getenv("DEEPSEEK_API_KEY")
    ?: ""

android {
    namespace = "com.example.projectbmi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.projectbmi"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "DEEPSEEK_API_KEY", "\"$DEEPSEEK_API_KEY\"")
        }

        getByName("debug") {
            buildConfigField("String", "DEEPSEEK_API_KEY", "\"$DEEPSEEK_API_KEY\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    composeOptions {
        // Match Compose Compiler to the BOM; adjust if you use a different Compose version
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    buildFeatures {
        compose = true
        buildConfig = true   // <-- Tambahkan yang ini
    }
}
