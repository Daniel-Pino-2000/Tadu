# Tadu ProGuard / R8 rules
#
# Firebase and AndroidX ship their own consumer rules. The rules below cover
# model classes that are read/written through reflection by Firestore.
#
# Docs: https://developer.android.com/build/shrink-code

# Firestore maps documents to data classes via reflection.
-keepclassmembers class com.myapp.tadu.data.** {
    *;
}
-keep class com.myapp.tadu.data.remote.User { *; }

# Keep annotations and generic signatures used by reflection-based libraries.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Preserve line numbers so release crash reports remain useful.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
