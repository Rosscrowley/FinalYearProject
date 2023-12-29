import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
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
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.2.0")
    implementation("com.google.cloud:google-cloud-speech:2.2.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:0.26.0")
    implementation("io.grpc:grpc-okhttp:1.40.0")
    implementation("io.grpc:grpc-stub:1.45.1")
    implementation("com.google.api:gax:1.65.0")
    implementation("com.google.firebase:firebase-database:20.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.google.android.gms:play-services-auth:20.5.0")
    implementation ("com.google.cloud:google-cloud-texttospeech:2.6.0")
    implementation ("com.google.auth:google-auth-library-credentials:0.23.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0-RC2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0-rc02")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0-rc02")

}