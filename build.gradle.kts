buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        // Note: Add any other classpath dependencies your project needs here
    }
}

plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}

