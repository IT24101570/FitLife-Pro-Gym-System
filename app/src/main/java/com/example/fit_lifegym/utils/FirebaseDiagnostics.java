package com.example.fit_lifegym.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Utility class for diagnosing Firebase connectivity and configuration issues
 */
public class FirebaseDiagnostics {
    
    private static final String TAG = "FirebaseDiagnostics";
    
    /**
     * Run comprehensive Firebase diagnostics
     * Call this from your Application onCreate or any Activity to debug issues
     */
    public static void runDiagnostics(Context context) {
        Log.d(TAG, "========== FIREBASE DIAGNOSTICS START ==========");
        
        // 1. Check Firebase initialization
        checkFirebaseInitialization();
        
        // 2. Check internet connectivity
        checkInternetConnection(context);
        
        // 3. Check database reference
        checkDatabaseReference();
        
        // 4. Test database connection
        testDatabaseConnection();
        
        Log.d(TAG, "========== FIREBASE DIAGNOSTICS END ==========");
    }
    
    private static void checkFirebaseInitialization() {
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            Log.d(TAG, "✓ Firebase is initialized");
            Log.d(TAG, "  App name: " + app.getName());
            Log.d(TAG, "  Options: " + app.getOptions().toString());
        } catch (Exception e) {
            Log.e(TAG, "✗ Firebase NOT initialized: " + e.getMessage());
        }
    }
    
    private static void checkInternetConnection(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                
                if (isConnected) {
                    Log.d(TAG, "✓ Internet connection available");
                    Log.d(TAG, "  Type: " + activeNetwork.getTypeName());
                } else {
                    Log.e(TAG, "✗ No internet connection");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "✗ Error checking internet: " + e.getMessage());
        }
    }
    
    private static void checkDatabaseReference() {
        try {
            FirebaseDatabase database = FirebaseHelper.getDatabase();
            Log.d(TAG, "✓ Database instance obtained");
            Log.d(TAG, "  Reference: " + database.getReference().toString());
            
            DatabaseReference usersRef = FirebaseHelper.getUsersRef();
            Log.d(TAG, "✓ Users reference created");
            Log.d(TAG, "  Path: " + usersRef.toString());
        } catch (Exception e) {
            Log.e(TAG, "✗ Error getting database reference: " + e.getMessage(), e);
        }
    }
    
    private static void testDatabaseConnection() {
        try {
            DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
            connectedRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        Log.d(TAG, "✓ Connected to Firebase Realtime Database");
                    } else {
                        Log.w(TAG, "✗ Not connected to Firebase Realtime Database");
                    }
                }
                
                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    Log.e(TAG, "✗ Connection test cancelled: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "✗ Error testing connection: " + e.getMessage(), e);
        }
    }
    
    /**
     * Test writing to database
     */
    public static void testDatabaseWrite(String testPath, String testValue) {
        Log.d(TAG, "Testing database write to: " + testPath);
        
        DatabaseReference testRef = FirebaseHelper.getDbRef().child(testPath);
        testRef.setValue(testValue)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "✓ Test write successful to: " + testPath);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "✗ Test write failed: " + e.getMessage(), e);
            });
    }
}
