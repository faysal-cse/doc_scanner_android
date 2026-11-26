-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.docscanner.app.**$$serializer { *; }
-keepclassmembers class com.docscanner.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.docscanner.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
