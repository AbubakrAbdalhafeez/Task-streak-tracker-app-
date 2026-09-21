# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Moshi & Serialization
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-keep class com.squareup.moshi.** { *; }

# Google APIs & OAuth
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.drive.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.common.base.**

# Firebase & Play Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
