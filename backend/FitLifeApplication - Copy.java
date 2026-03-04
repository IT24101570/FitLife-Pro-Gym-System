package com.example.fit_lifegym;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

public class FitLifeApplication extends Application {
    
    public static final String DATABASE_URL = "https://fit-life-gym-default-rtdb.firebaseio.com";

    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Precise configuration from your google-services.json
            FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("1:990203577736:android:fada0e3b0ce257b4e6263d")
                .setProjectId("fit-life-gym")
                .setDatabaseUrl(DATABASE_URL)
                .setApiKey("AIzaSyAyP9BkEAZ79iROXARNlE9DEHAugd652ws")
                .build();

            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this, options);
            }
            
            // Critical: Enable persistence on the SPECIFIC instance
            FirebaseDatabase.getInstance(DATABASE_URL).setPersistenceEnabled(true);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        ThemeManager themeManager = new ThemeManager(this);
        themeManager.applySavedTheme();
    }
}
