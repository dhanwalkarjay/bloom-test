# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit / Gson / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn com.google.gson.**

# Keep models for Gson deserialization
-keep class com.bloom.customer.data.model.** { *; }

# SceneView / Filament
-keep class io.github.sceneview.** { *; }
-keep class com.google.android.filament.** { *; }

# Razorpay
-keep class com.razorpay.** {*;}
-dontwarn com.razorpay.**

# Suppress Supabase / Realtime warnings if any
-dontwarn io.supabase.**
