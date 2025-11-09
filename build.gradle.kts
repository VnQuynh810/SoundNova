
buildscript {
    extra.apply {
        set("room_version", "2.5.2")
    }
//    dependencies {
//        classpath("androidx.navigation:navigation-safe-args-gradle-plugin")
//    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}