package com.dashwood.photodownloader.data;

import android.content.Context;
import android.content.SharedPreferences;

public class Data {
    public static void saveToPreferenceString(Context context, String preferenceHome, String preferenceValue, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceHome, preferenceValue);
        editor.apply();
    }

    public static String readPreferencesString(Context context, String preferenceHome, String defaultValue, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceHome, defaultValue);
    }
}
