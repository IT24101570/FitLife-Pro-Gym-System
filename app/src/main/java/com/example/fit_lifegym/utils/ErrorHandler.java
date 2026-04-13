package com.example.fit_lifegym.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseError;

/**
 * Centralized error handling utility
 */
public class ErrorHandler {

    private static final String TAG = "ErrorHandler";

    /**
     * Handle Firebase Auth errors with user-friendly messages
     */
    public static String getAuthErrorMessage(Exception exception) {
        if (exception == null) {
            return "An unknown error occurred";
        }

        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) exception;
            String errorCode = authException.getErrorCode();
            
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    return "Invalid email address";
                case "ERROR_WRONG_PASSWORD":
                    return "Incorrect password";
                case "ERROR_USER_NOT_FOUND":
                    return "No account found with this email";
                case "ERROR_USER_DISABLED":
                    return "This account has been disabled";
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "Email is already registered";
                case "ERROR_WEAK_PASSWORD":
                    return "Password is too weak";
                case "ERROR_TOO_MANY_REQUESTS":
                    return "Too many attempts. Please try again later";
                default:
                    return "Authentication failed: " + authException.getMessage();
            }
        }

        if (exception instanceof FirebaseNetworkException) {
            return "Network error. Please check your connection";
        }

        return exception.getMessage() != null ? exception.getMessage() : "An error occurred";
    }

    /**
     * Handle Firebase Database errors
     */
    public static String getDatabaseErrorMessage(DatabaseError error) {
        if (error == null) {
            return "Database error occurred";
        }

        switch (error.getCode()) {
            case DatabaseError.PERMISSION_DENIED:
                return "Permission denied. Please check your access rights";
            case DatabaseError.NETWORK_ERROR:
                return "Network error. Please check your connection";
            case DatabaseError.DISCONNECTED:
                return "Connection lost. Please try again";
            case DatabaseError.EXPIRED_TOKEN:
                return "Session expired. Please login again";
            default:
                return "Database error: " + error.getMessage();
        }
    }

    /**
     * Log and show error to user
     */
    public static void handleError(Context context, String tag, String operation, Exception exception) {
        String message = getAuthErrorMessage(exception);
        Log.e(tag != null ? tag : TAG, operation + " failed: " + message, exception);
        
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Log and show database error to user
     */
    public static void handleDatabaseError(Context context, String tag, String operation, DatabaseError error) {
        String message = getDatabaseErrorMessage(error);
        Log.e(tag != null ? tag : TAG, operation + " failed: " + message);
        
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show generic error message
     */
    public static void showError(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show success message
     */
    public static void showSuccess(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
