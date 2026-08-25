import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android")

    // Uncomment when google-services.json is added
    id("com.google.gms.google-services")
    // id("com.google.firebase.crashlytics")
}

// Load local.properties for API keys
val localProperties = Properties().apply {
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        load(localPropsFile.inputStream())
    }
}

android {
    namespace = "com.bloom"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bloom"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject API keys from local.properties into BuildConfig
        buildConfigField("String", "SUPABASE_URL",
            "\"${localProperties.getProperty("SUPABASE_URL", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY",
            "\"${localProperties.getProperty("SUPABASE_ANON_KEY", "")}\"")
        buildConfigField("String", "RAZORPAY_KEY_ID",
            "\"${localProperties.getProperty("RAZORPAY_KEY_ID", "")}\"")

        // Maps API key via manifestPlaceholders
        manifestPlaceholders["MAPS_API_KEY"] =
            localProperties.getProperty("MAPS_API_KEY", "")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    lint {
        abortOnError = false
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
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
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Scalable sizes for responsive design
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.intuit.ssp:ssp-android:1.1.1")

    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Logging & Analytics
    implementation("com.jakewharton.timber:timber:5.0.1")
    // Firebase & Google Sign-In
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    
    // Truecaller
    implementation("com.truecaller.android.sdk:truecaller-sdk:3.0.0")

    // Supabase
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.0")

    // Security & Local Data
    implementation(libs.security.crypto)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // UI & Images
    implementation(libs.glide)
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.airbnb.android:lottie:6.4.0")

    // Location & Maps
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.swiperefreshlayout)

    // Payments
    implementation(libs.razorpay)

    // 3D Rendering (Sceneview / Filament)
    implementation("io.github.sceneview:sceneview:2.0.3")

    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
