plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id ("kotlin-kapt")
}

android {
    namespace = "com.example.finalyearproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.finalyearproject"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packagingOptions() {
        exclude("META-INF/INDEX.LIST")
        exclude("META-INF/DEPENDENCIES")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }

    packaging {
        resources.excludes.add("META-INF/INDEX.LIST")
        resources.excludes.add("META-INF/DEPENDENCIES")
    }


}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.cloud:google-cloud-speech:2.2.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.12.1")
    implementation("io.grpc:grpc-okhttp:1.40.0")
    implementation("io.grpc:grpc-stub:1.50.1")
    implementation("com.google.api:gax:2.19.4")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("androidx.camera:camera-core:1.3.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.google.android.gms:play-services-auth:21.0.0")
    implementation ("com.google.cloud:google-cloud-texttospeech:2.6.0")
    implementation ("com.google.auth:google-auth-library-credentials:1.12.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0-RC2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0-alpha01")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0-alpha01")
    implementation ("org.jsoup:jsoup:1.17.2")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation ("com.arthenica:mobile-ffmpeg-full:4.4.LTS")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

// Optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:2.6.1")
}