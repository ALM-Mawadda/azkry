# --- Kotlin reflection / metadata ---
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations
-keepattributes Signature,InnerClasses,EnclosingMethod
-keep class kotlin.Metadata { *; }

# --- Kotlinx Serialization (used by core/models and settings payloads) ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep generated $$serializer companions and serialize/deserialize members for any @Serializable model
-keep,includedescriptorclasses class com.azkry.app.**$$serializer { *; }
-keepclassmembers class com.azkry.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.azkry.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Hilt / Dagger ---
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclassmembers class ** {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}
-keepclassmembers class ** {
    @javax.inject.Inject <init>(...);
}

# --- Coroutines (continuation classes) ---
-keepnames class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- Room ---
-keep class androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# --- Compose tooling artefacts that R8 sometimes flags ---
-dontwarn androidx.compose.**

# --- Project: keep domain models ---
-keep class com.azkry.app.core.models.** { *; }
-keep class com.azkry.app.**.models.** { *; }
