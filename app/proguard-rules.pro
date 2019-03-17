# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
-dontoptimize
-dontpreverify

# This is needed to allow release builds from android studio.
-ignorewarnings

-keepclassmembers enum * { *; }

# Keep enough data for stack traces
-keepnames class **
-renamesourcefileattribute SourceFile
-keepattributes Signature,InnerClasses,SourceFile,LineNumberTable,*Annotation*

# Keep the static fields of referenced inner classes of auto-generated R classes, in case we
# access those fields by reflection (e.g. EmojiMarkup)
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep all libraries
-keep class android.support.** { *; }
-keep class android.arch.** { *; }
-keep class com.google.** { *; }
-keep class com.jakewharton.** { *; }
-keep class org.joda.** { *; }
-keep interface org.joda.** { *; }

# Hide warnings
-dontwarn android.support.**
-dontwarn android.arch.**
-dontwarn com.google.**
-dontwarn com.jakewharton.**
-dontwarn org.joda.**