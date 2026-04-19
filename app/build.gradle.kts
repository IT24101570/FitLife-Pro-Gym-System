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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    lint {
        abortOnError = false
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/res/assets")
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    // Biometric
    implementation(libs.androidx.biometric)
    
    // Firebase BOM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.storage)
    
    // Google Play Services & Health
    implementation(libs.play.services.fitness)
    implementation(libs.play.services.auth)
    
    // Refresh Layout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
    // Calendar View
    implementation(libs.calendar.view)
    
    // CardView
    implementation(libs.androidx.cardview)
    
    // ViewPager2
    implementation(libs.androidx.viewpager2)
    
    // Preference
    implementation(libs.androidx.preference)
    
    // Image Loading
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    
    // Charts
    implementation(libs.mp.android.chart)
    
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    
    // Room
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    
    // WorkManager
    implementation(libs.androidx.work.runtime)

    // WebRTC
    implementation(libs.webrtc)
    
    // PermissionX
    implementation(libs.permissionx)

    // YouTube Player
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    // Cloudinary
    implementation(libs.cloudinary)
    implementation(libs.cloudinary.preprocess)
    implementation(libs.cloudinary.download)

    // Hilt
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}