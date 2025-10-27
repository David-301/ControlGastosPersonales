plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.ch220048.controlgastospersonales"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ch220048.controlgastospersonales"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)  // Ya incluye Material Components
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // MPAndroidChart para gráficos
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


}