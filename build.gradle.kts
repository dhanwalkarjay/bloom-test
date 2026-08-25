// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    
    // Uncomment when google-services.json is added
    id("com.google.gms.google-services") version "4.4.1" apply false
    // id("com.google.firebase.crashlytics") version "2.9.9" apply false
}