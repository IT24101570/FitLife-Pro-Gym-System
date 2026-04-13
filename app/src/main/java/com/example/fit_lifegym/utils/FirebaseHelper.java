package com.example.fit_lifegym.utils;

import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static final String DB_URL = "https://fit-life-gym-default-rtdb.firebaseio.com";
    private static final String STORAGE_BUCKET = "fit-life-gym.firebasestorage.app";
    private static FirebaseDatabase databaseInstance;
    private static FirebaseStorage storageInstance;

    public static FirebaseDatabase getDatabase() {
        if (databaseInstance == null) {
            try {
                // Initialize with explicit URL for the default instance
                databaseInstance = FirebaseDatabase.getInstance(DB_URL);
                databaseInstance.setPersistenceEnabled(true);
                Log.d(TAG, "Firebase Database initialized with URL: " + DB_URL);
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Database: " + e.getMessage());
                databaseInstance = FirebaseDatabase.getInstance();
            }
        }
        return databaseInstance;
    }

    public static FirebaseStorage getStorage() {
        if (storageInstance == null) {
            try {
                // Use the bucket from google-services.json
                storageInstance = FirebaseStorage.getInstance("gs://" + STORAGE_BUCKET);
                Log.d(TAG, "Firebase Storage initialized with bucket: " + STORAGE_BUCKET);
            } catch (Exception e) {
                Log.e(TAG, "Fallback to default storage instance: " + e.getMessage());
                storageInstance = FirebaseStorage.getInstance();
            }
        }
        return storageInstance;
    }

    public static DatabaseReference getDbRef() {
        return getDatabase().getReference();
    }

    public static DatabaseReference getUsersRef() {
        return getDbRef().child("users");
    }

    public static DatabaseReference getBookingsRef() {
        return getDbRef().child("bookings");
    }
    
    public static DatabaseReference getProfessionalsRef() {
        return getDbRef().child("professionals");
    }

    public static DatabaseReference getWorkoutsRef(String userId) {
        return getDbRef().child("workoutHistory").child(userId);
    }
    
    public static DatabaseReference getWorkoutsRef() {
        return getDbRef().child("workoutHistory");
    }

    public static DatabaseReference getSubscriptionsRef(String userId) {
        return getDbRef().child("subscriptions").child(userId);
    }

    public static DatabaseReference getSubscriptionsRef() {
        return getDbRef().child("subscriptions");
    }

    public static DatabaseReference getPostsRef() {
        return getDbRef().child("posts");
    }

    public static DatabaseReference getExerciseLibraryRef() {
        return getDbRef().child("exerciseLibrary");
    }

    public static DatabaseReference getNutritionLogsRef(String userId) {
        return getDbRef().child("nutritionLogs").child(userId);
    }

    public static DatabaseReference getNutritionLogsRef() {
        return getDbRef().child("nutritionLogs");
    }
    
    public static DatabaseReference getPaymentsRef(String userId) {
        return getDbRef().child("payments").child(userId);
    }
    
    public static DatabaseReference getPaymentsRef() {
        return getDbRef().child("payments");
    }
    
    public static DatabaseReference getVideoClassesRef() {
        return getDbRef().child("videoClasses");
    }
}
