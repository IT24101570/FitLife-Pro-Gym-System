plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.fit_lifegym"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fit_lifegym"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Biometric
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    
    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-storage")
    
    // Google Play Services & Health
    implementation(libs.play.services.fitness)
    implementation(libs.play.services.auth)
    
    // Refresh Layout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
    // Calendar View
    implementation("com.applandeo:material-calendar-view:1.9.0-rc03")
    
    // CardView
    implementation("androidx.cardview:cardview:1.0.0")
    
    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    
    // Preference
    implementation("androidx.preference:preference:1.2.1")
    
    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.gms.play.services.fitness)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    
    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    
    // WorkManager
    implementation("androidx.work:work-runtime:2.9.0")

    // WebRTC - Switched from JitPack to Maven Central for stability and sources
    implementation("io.github.webrtc-sdk:android:104.5112.01")
    
    // PermissionX
    implementation("com.guolindev.permissionx:permissionx:1.7.1")

    // Hilt
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}