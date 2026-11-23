# Official Huawei HMS Core / Account Kit (hwid) R8/ProGuard keep rules
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

-keep class com.huawei.hms.** { *; }
-keep interface com.huawei.hms.** { *; }
-keep class com.huawei.hmf.** { *; }
-keep interface com.huawei.hmf.** { *; }
-keep class com.huawei.agconnect.** { *; }
-keep class com.mekfeya.hmssmsbridge.** { *; }
