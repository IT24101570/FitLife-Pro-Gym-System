package com.example.fit_lifegym.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "FitLifeSession";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";
    private static final String KEY_PROFILE_IMAGE = "profileImage";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_IS_PREMIUM = "isPremium";
    private static final String KEY_FINGERPRINT_ENABLED = "fingerprintEnabled";

    private static final String KEY_PASSWORD = "password";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void setFingerprintEnabled(boolean enabled) {
        editor.putBoolean(KEY_FINGERPRINT_ENABLED, enabled);
        editor.apply();
    }

    public boolean isFingerprintEnabled() {
        return prefs.getBoolean(KEY_FINGERPRINT_ENABLED, false);
    }

    public void createLoginSession(String userId, String name, String email, String phone, String role, String password) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_PASSWORD, password);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public String getPassword() {
        return prefs.getString(KEY_PASSWORD, null);
    }

    public void saveUserDetails(String userId, String name, String email, String phone, String role, String profileImage) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_PROFILE_IMAGE, profileImage);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void setPremium(boolean isPremium) {
        editor.putBoolean(KEY_IS_PREMIUM, isPremium);
        editor.apply();
    }

    public boolean isPremiumUser() {
        return prefs.getBoolean(KEY_IS_PREMIUM, false);
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "MEMBER");
    }

    public String getProfileImage() {
        return prefs.getString(KEY_PROFILE_IMAGE, null);
    }

    public void setProfileImage(String profileImage) {
        editor.putString(KEY_PROFILE_IMAGE, profileImage);
        editor.apply();
    }

    public void updateUserInfo(String name, String phone) {
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PHONE, phone);
        editor.apply();
    }

    public void logout() {
        // Preserve fingerprint preference and email for biometric login
        boolean fingerprintWasEnabled = isFingerprintEnabled();
        String lastEmail = getEmail();
        String lastPassword = getPassword();
        
        editor.clear();
        editor.putBoolean(KEY_FINGERPRINT_ENABLED, fingerprintWasEnabled);
        editor.putString(KEY_EMAIL, lastEmail);
        editor.putString(KEY_PASSWORD, lastPassword);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    public boolean isMember() {
        return "MEMBER".equals(getRole());
    }

    public boolean isTrainer() {
        return "TRAINER".equals(getRole());
    }

    public boolean isDoctor() {
        return "DOCTOR".equals(getRole());
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }
}
