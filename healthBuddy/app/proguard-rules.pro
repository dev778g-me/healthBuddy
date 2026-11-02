# Keep the scan pipeline and data models (kotlinx.serialization needs these).
-keep class org.dev.healthbuddy.scan.** { *; }
-keep class org.dev.healthbuddy.data.** { *; }
-keep class org.dev.healthbuddy.domain.** { *; }
-keep class org.dev.healthbuddy.ui.** { *; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions
-keepclassmembers class * {
  kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class **$$serializer { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }

# ML Kit text recognition (model loading is reflection-based)
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.datatransport.** { *; }

# org.json + networking
-keep class org.json.** { *; }

-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**
