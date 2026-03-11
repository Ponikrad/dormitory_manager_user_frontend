package com.example.dormmanager.utils;

public class Constants {

    public static final String BASE_URL = "http://10.0.2.2:8080/";
    //public static final String BASE_URL = "https://bryological-maryjo-soothingly.ngrok-free.dev";

    public static final String PREF_NAME = "DormManagerPrefs";
    public static final String KEY_TOKEN = "jwt_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_PHONE_NUMBER = "phone_number";
    public static final String KEY_ROOM_NUMBER = "room_number";
    public static final String KEY_ROLE = "role";
    public static final String KEY_CREATED_AT = "created_at";
    public static final String KEY_IS_ACTIVE = "is_active";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";

    public static final int REQUEST_CODE_LOGIN = 1001;
    public static final int REQUEST_CODE_REGISTER = 1002;

    public static final int SPLASH_DURATION = 2000; // 2 seconds

    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MIN_USERNAME_LENGTH = 3;
}