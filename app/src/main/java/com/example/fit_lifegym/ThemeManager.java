package com.example.fit_lifegym;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    public static final int MODE_LIGHT = 0;
    public static final int MODE_DARK = 1;
    public static final int MODE_SYSTEM = 2;
    
    private final SharedPreferences prefs;
    
    public ThemeManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
        applyTheme(mode);
    }
    
    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, MODE_DARK);
    }
    
    public void applyTheme(int mode) {
        switch (mode) {
            case MODE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case MODE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case MODE_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
    
    public void applySavedTheme() {
        applyTheme(getThemeMode());
    }
    
    public String getThemeModeName(int mode) {
        switch (mode) {
            case MODE_LIGHT: return "Light Mode";
            case MODE_DARK: return "Dark Mode";
            case MODE_SYSTEM: return "Follow System";
            default: return "Default";
        }
    }
}
