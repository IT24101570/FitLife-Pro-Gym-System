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
    }
    
    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.material)
    
    // Biometric
    
    // Firebase BOM
    
    // Google Play Services & Health
    implementation(libs.play.services.fitness)
    implementation(libs.play.services.auth)
    
    // Refresh Layout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
    // Calendar View
    
    // CardView
    
    // ViewPager2
    
    // Preference
    
    // Image Loading
    
    // Charts
    
    // Retrofit
    
    // Room
    
    // WorkManager

    
    // PermissionX

    // Hilt
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler)
    
    testImplementation(libs.junit)
}