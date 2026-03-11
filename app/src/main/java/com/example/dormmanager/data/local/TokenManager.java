package com.example.dormmanager.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.dormmanager.data.model.User;
import com.example.dormmanager.utils.Constants;


public class TokenManager {
    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(Constants.KEY_TOKEN, token).apply();
        prefs.edit().putBoolean(Constants.KEY_IS_LOGGED_IN, true).apply();
    }

    public String getToken() {
        return prefs.getString(Constants.KEY_TOKEN, null);
    }

    public void saveUser(User user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Constants.KEY_USER_ID, user.getId());
        editor.putString(Constants.KEY_USERNAME, user.getUsername());
        editor.putString(Constants.KEY_EMAIL, user.getEmail());
        editor.putString(Constants.KEY_FIRST_NAME, user.getFirstName());
        editor.putString(Constants.KEY_LAST_NAME, user.getLastName());
        editor.putString(Constants.KEY_PHONE_NUMBER, user.getPhoneNumber());
        editor.putString(Constants.KEY_ROOM_NUMBER, user.getRoomNumber());
        editor.putString(Constants.KEY_ROLE, user.getRole());
        editor.putString(Constants.KEY_CREATED_AT, user.getCreatedAt());
        editor.putBoolean(Constants.KEY_IS_ACTIVE, user.isActive());
        editor.apply();
    }

    public String getUsername() {
        return prefs.getString(Constants.KEY_USERNAME, null);
    }

    public String getEmail() {
        return prefs.getString(Constants.KEY_EMAIL, null);
    }

    public String getFirstName() {
        return prefs.getString(Constants.KEY_FIRST_NAME, null);
    }

    public String getLastName() {
        return prefs.getString(Constants.KEY_LAST_NAME, null);
    }

    public String getPhoneNumber() {
        return prefs.getString(Constants.KEY_PHONE_NUMBER, null);
    }

    public String getRoomNumber() {
        return prefs.getString(Constants.KEY_ROOM_NUMBER, null);
    }

    public String getCreatedAt() {
        return prefs.getString(Constants.KEY_CREATED_AT, null);
    }

    public String getRole() {
        return prefs.getString(Constants.KEY_ROLE, "STUDENT");
    }

    public boolean isActive() {
        return prefs.getBoolean(Constants.KEY_IS_ACTIVE, true);
    }

    public String getFullName() {
        String firstName = getFirstName();
        String lastName = getLastName();
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return getUsername();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false) && getToken() != null;
    }
}