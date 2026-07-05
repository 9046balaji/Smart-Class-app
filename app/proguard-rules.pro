# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Protect network Data Transfer Objects (DTOs) from name optimization
-keep class com.vfstr.smartclass.data.remote.api.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

# Retain Room Database serialization structures
-keep class com.vfstr.smartclass.data.local.db.** { *; }
-dontwarn androidx.room.paging.**

# Apache POI rules for Excel Timetable & MOOC Parsing
-dontwarn org.apache.poi.**
-dontwarn org.openxmlformats.schemas.**
-keeppackagenames org.apache.poi.ss.formula.function.
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.schemas.** { *; }

# Retain analytics dataset schemas loaded from local assets
-keep class com.vfstr.smartclass.domain.models.** { *; }

# Retain Line Number attributes for de-obfuscated crash debugging logs
-keepattributes SourceFile, LineNumberTable
