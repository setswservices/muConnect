package com.sanahealth.SanaMaskv3;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

class Utils {
    static private final String preferenceName = "com.sanahealth.SanaMaskv3.appPreferences";
    static void setPreference(Context context, String name, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    static boolean getPreference(Context context, String name, boolean defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);

        return preferences.getBoolean(name, defaultValue);
    }
}
