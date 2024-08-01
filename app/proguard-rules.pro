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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Add this global rule
-keepattributes Signature

# This rule will properly ProGuard all the model classes in
# the package grid.pos.data Modify
#-keep class com.grid.pos.data.** { *; }
-keepclassmembers class com.grid.pos.data.** { *; }

#-keep class com.aspose.cells.Workbook { *; }
#-keep class com.aspose.cells.Worksheet { *; }
#-keep class com.aspose.cells.FileFormatType { *; }
#
#
#-dontwarn com.aspose.cells.a.*
#-dontwarn com.aspose.cells.c.*

-keep class org.apache.poi.** { *; }
-keep class org.apache.commons.** { *; }
-keep class org.openxmlformats.schemas.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class com.microsoft.schemas.office.** { *; }
-keep class aQute.bnd.annotation.spi.** { *; }
-keep class java.awt.** { *; }
-keep class org.osgi.framework.** { *; }
-keep class org.apache.logging.log4j.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.apache.commons.**
-dontwarn org.openxmlformats.schemas.**
-dontwarn org.apache.xmlbeans.**
-dontwarn com.microsoft.schemas.office.**
-dontwarn aQute.bnd.annotation.spi.office.**
-dontwarn java.awt.**
-dontwarn org.osgi.framework.**
-dontwarn org.apache.logging.log4j.**

-dontwarn net.sourceforge.jtds.**
