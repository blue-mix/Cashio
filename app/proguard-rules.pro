#############################################################
## Cashio - Optimized R8 / ProGuard Rules
#############################################################
#
## --------------------------------------------------------------------------
## 1. Optimization & Code Stripping
## --------------------------------------------------------------------------
## Strip debug logging calls from Release builds.
## This removes the code and the strings associated with logs.
#-assumenosideeffects class android.util.Log {
#    public static boolean isLoggable(java.lang.String, int);
#    public static int v(...);
#    public static int d(...);
#    public static int i(...);
#    public static int w(...);
#}
#
## Remove debugging attributes to save space.
## WARNING: Crash reports will not show line numbers.
## If you need Crashlytics to be readable, comment out the next line.
#-renamesourcefileattribute ''
#
## Only keep what is strictly necessary for annotation processing
#-keepattributes *Annotation*,Signature,Exceptions,InnerClasses
#
## --------------------------------------------------------------------------
## 2. Kotlinx Serialization (Specific)
## --------------------------------------------------------------------------
## Only keep `serializer()` on the companion object if the class itself is used.
#-if @kotlinx.serialization.Serializable class *
#-keepclassmembers class **$Companion {
#    kotlinx.serialization.KSerializer serializer();
#}
#
## Keep the primary constructor for serialization
#-keepclassmembers class * {
#    @kotlinx.serialization.Serializable <init>(...);
#}
#
## --------------------------------------------------------------------------
## 3. Navigation Compose (Type-Safe Routes)
## --------------------------------------------------------------------------
## Keep the Route class names so serialization can match them,
## but allow R8 to shrink unused methods inside them.
#-keepnames class com.bluemix.cashio.ui.navigation.Route
#-keepnames class com.bluemix.cashio.ui.navigation.Route$*
#
## --------------------------------------------------------------------------
## 4. Realm Kotlin (Optimized)
## --------------------------------------------------------------------------
## Instead of keeping the entire Domain layer, we strictly keep the Entity fields
## because Realm uses reflection/JNI to map these specific fields to the DB.
## We allow obfuscation of methods but keep fields and constructors.
#
#-keepnames class com.bluemix.cashio.data.local.entity.**
#-keepclassmembers class com.bluemix.cashio.data.local.entity.** {
#    # Keep fields so Realm finds the schema
#    <fields>;
#    # Keep empty constructor required by Realm
#    public <init>();
#}
#
## Suppress Realm internal warnings
#-dontwarn io.realm.kotlin.**
#
## --------------------------------------------------------------------------
## 5. Koin (Dependency Injection)
## --------------------------------------------------------------------------
## Koin is mostly reflection-free now. We don't need broad keep rules.
## Just ensure module definitions aren't stripped if defined implicitly.
#-dontwarn org.koin.**
#
## --------------------------------------------------------------------------
## 6. Library Hygiene (Charts & Calendar)
## --------------------------------------------------------------------------
## Prevent build warnings for optional dependencies we don't use
#-dontwarn com.kizitonwose.calendar.**
#-dontwarn com.ehsan.narmani.compose_charts.**
#-dontwarn java.time.**
#
## --------------------------------------------------------------------------
## 7. General enum safety
## --------------------------------------------------------------------------
## Enum.valueOf(String) is used by many JSON parsers and DBs.
#-keepclassmembers enum * {
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}

############################################################
# Cashio - Release Safety Rules
############################################################

# Keep crash line numbers (highly recommended)
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,Exceptions,InnerClasses

# Strip debug logs (optional)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

############################################################
# 1) Kotlinx Serialization (required for typed routes)
############################################################

# Keep serializer() on Companions for @Serializable classes
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class **$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep generated Serializers (sometimes stripped)
-keep class **$$serializer { *; }

# Keep @Serializable class members that are used for decoding
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    <fields>;
    <init>(...);
}

############################################################
# 2) Navigation Compose typed Routes (YOUR setup depends on class names)
############################################################

# You compare routes using qualifiedName, so don't let R8 rename them.
-keepnames class com.bluemix.cashio.ui.navigation.Route
-keepnames class com.bluemix.cashio.ui.navigation.Route$*

# Also keep the nested route classes themselves (names + metadata)
-keep class com.bluemix.cashio.ui.navigation.Route$* { *; }

############################################################
# 3) Realm Kotlin
############################################################

# Keep Realm model classes (safe default)
-keep class ** implements io.realm.kotlin.types.RealmObject { *; }
-keepclassmembers class ** implements io.realm.kotlin.types.RealmObject {
    <fields>;
}
-dontwarn io.realm.kotlin.**

############################################################
# 4) Koin (generally fine without keep rules)
############################################################
-dontwarn org.koin.**

############################################################
# 5) Enum safety
############################################################
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

############################################################
# 6) Optional: suppress warnings only if you see them
############################################################
# -dontwarn com.kizitonwose.calendar.**
# -dontwarn io.github.ehsannarmani.**
