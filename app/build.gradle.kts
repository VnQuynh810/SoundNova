
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
//    id ("androidx.navigation.safeargs")

}
android {
    namespace = "com.example.soundnova"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.soundnova"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Sử dụng androidx.test.runner.AndroidJUnitRunner thay vì android.support.test.runner.AndroidJUnitRunner
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    configurations {
        all {
            exclude(group = "com.google.protobuf", module = "protobuf-lite")
        }
    }
    // Các thư viện AndroidX và Firebase
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
//    implementation("com.google.code.gson:gson:2.10.1")

    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore.ktx)

    implementation(libs.firebase.database.ktx)
//    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.espresso.intents)
    implementation(libs.androidx.ui.test.android)

    implementation(libs.androidx.fragment.testing)
    implementation(libs.androidx.espresso.contrib)
    testImplementation(libs.junit)
    implementation (libs.androidx.media)
    // Thư viện kiểm thử
    testImplementation("junit:junit:4.13.2") // JUnit cho test đơn vị
//    testImplementation("org.mockito:mockito-core:4.2.0") // Mockito cho mock test
    testImplementation ("org.mockito:mockito-android:4.0.0")
    testImplementation ("io.mockk:mockk:1.13.4")
    testImplementation("org.mockito:mockito-inline:4.2.0")
    // Thư viện kiểm thử Android Instrumentation
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation ("androidx.test.espresso:espresso-idling-resource:3.5.1")
    implementation ("com.github.bumptech.glide:glide:4.13.0") // Glide library
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.0") // Glide annotation processor
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Thư viện dành cho chế độ debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation ("androidx.fragment:fragment-testing:1.5.7")


    // AndroidX Test Core
    androidTestImplementation("androidx.test:core:1.5.0")
    // AndroidX Test Rules
    androidTestImplementation("androidx.test:rules:1.5.0")
    // AndroidX Test Runner
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("org.mockito:mockito-android:4.2.0") // Dành cho các test Android Instrumentation


    // Chạy tập lệnh Kotlin
    implementation(kotlin("script-runtime"))
    implementation("com.google.android.material:material:1.12.0")

    // API
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("androidx.room:room-ktx:${rootProject.extra["room_version"]}")
    implementation("androidx.room:room-runtime:${rootProject.extra["room_version"]}")
    ksp("androidx.room:room-compiler:${rootProject.extra["room_version"]}")

    //hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.8.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.4")

    implementation ("androidx.appcompat:appcompat:1.6.1")

    //API lyric
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.3")
}