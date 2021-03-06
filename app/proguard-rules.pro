# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


-dontwarn org.fusesource.hawtdispatch.**
-dontwarn com.github.siyamed.shapeimageview.path.parser.**
-dontwarn com.squareup.picasso.**
-dontwarn okio.**
-dontwarn com.squareup.javawriter.JavaWriter
-dontwarn android.net.http.**
-dontwarn com.android.internal.http.multipart.**

#---------------- google ---------

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**





# ------------------- TEST DEPENDENCIES -------------------
-dontwarn org.hamcrest.**
-dontwarn android.test.**
-dontwarn android.support.test.**


-keep class org.hamcrest.** {
   *;
}

-keep class org.junit.** { *; }
-dontwarn org.junit.**

-keep class junit.** { *; }
-dontwarn junit.**

#-------------------------
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable


#--------- butterknife ------
# Retain generated class which implement Unbinder.
-keep public class * implements butterknife.Unbinder { public <init>(**, android.view.View); }

# Prevent obfuscation of types which use ButterKnife annotations since the simple name
# is used to reflectively look up the generated ViewBinding.
-keep class butterknife.*
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }
-keep class **$$ViewBinder { *; }

-dontwarn butterknife.internal.**


#----------- remove all Log.d code -----
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** i(...);
}




