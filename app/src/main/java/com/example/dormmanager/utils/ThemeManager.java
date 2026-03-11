package com.example.dormmanager.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_THEME = "theme_mode";
    private static final int THEME_LIGHT = 0;
    private static final int THEME_DARK = 1;

    private final SharedPreferences prefs;

    public ThemeManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void toggleTheme() {
        int currentTheme = prefs.getInt(KEY_THEME, THEME_LIGHT);
        int newTheme = (currentTheme == THEME_LIGHT) ? THEME_DARK : THEME_LIGHT;
        setTheme(newTheme);
    }

    public void setTheme(int theme) {
        prefs.edit().putInt(KEY_THEME, theme).apply();
        applyTheme(theme);
    }

    public void applyTheme(int theme) {
        if (theme == THEME_DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public int getCurrentTheme() {
        return prefs.getInt(KEY_THEME, THEME_LIGHT);
    }

    public boolean isDarkMode() {
        return getCurrentTheme() == THEME_DARK;
    }

    public void applySavedTheme() {
        int theme = getCurrentTheme();
        applyTheme(theme);
    }
}