// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
//buildscript {
//    dependencies {
//        classpath("com.google.gms:google-services:4.4.2")
//    }
//}
//buildscript {
//    repositories {
//        google()
//        mavenCentral()
//    }
//    dependencies {
//        classpath (libs.google.services) // Add this line
//    }
//}
//plugins {
//    // ...
//
//    // Add the dependency for the Google services Gradle plugin
//    id("com.google.gms.google-services") version "4.4.2" apply false
//
//}