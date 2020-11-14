package com.mushin.muconnect;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class Utils {
    static private final String preferenceName = "com.mushin.muconnect.appPreferences";

    static public void setPreference(Context context, String name, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    static public void setPreference(Context context, String name, float value) {
        SharedPreferences preferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(name, value);
        editor.apply();
    }

    static public void setPreference(Context context, String name, int value) {
        SharedPreferences preferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(name, value);
        editor.apply();
    }

    static public void setPreference(Context context, String name, String value) {
        SharedPreferences preferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(name, value);
        editor.apply();
    }

    static public void storeDeviceConfiguration(Context context, Configuration config) {
        SharedPreferences preferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("cfg_dataInterval", config.getDataInterval());
        editor.putBoolean("cfg_temperatureDataEnabled", config.isTemperatureDataEnabled());
        editor.putBoolean("cfg_emulationEnabled", config.isEmulationEnabled());

        editor.putBoolean("cfg_leftCrankDataEnabled", config.isLeftCrankDataEnabled());
        editor.putBoolean("cfg_rightCrankDataEnabled", config.isRightCrankDataEnabled());
        editor.putBoolean("cfg_crankDataFilterEnabled", config.isCrankDataFilterEnabled());
        editor.putFloat("cfg_crankRawThreshold", config.getCrankRawThreshold());
        editor.putFloat("cfg_crankFilteredThreshold", config.getCrankFilteredThreshold());

        editor.putBoolean("cfg_accXDataEnabled", config.isAccXDataEnabled());
        editor.putBoolean("cfg_accYDataEnabled", config.isAccYDataEnabled());
        editor.putBoolean("cfg_accZDataEnabled", config.isAccZDataEnabled());
        editor.putBoolean("cfg_accRawDataEnabled", config.isAccRawDataEnabled());
        editor.putBoolean("cfg_accRawXDataEnabled", config.isAccRawXDataEnabled());
        editor.putBoolean("cfg_accRawYDataEnabled", config.isAccRawYDataEnabled());
        editor.putBoolean("cfg_accRawZDataEnabled", config.isAccRawZDataEnabled());
        editor.putBoolean("cfg_accXDataFilterEnabled", config.isAccXDataFilterEnabled());
        editor.putBoolean("cfg_accYDataFilterEnabled", config.isAccYDataFilterEnabled());
        editor.putBoolean("cfg_accZDataFilterEnabled", config.isAccZDataFilterEnabled());
        editor.putFloat("cfg_accXFilteredThreshold", config.getAccXFilteredThreshold());
        editor.putFloat("cfg_accYFilteredThreshold", config.getAccYFilteredThreshold());
        editor.putFloat("cfg_accZFilteredThreshold", config.getAccZFilteredThreshold());

        editor.putBoolean("cfg_gyroXDataEnabled", config.isGyroXDataEnabled());
        editor.putBoolean("cfg_gyroYDataEnabled", config.isGyroYDataEnabled());
        editor.putBoolean("cfg_gyroZDataEnabled", config.isGyroZDataEnabled());
        editor.putBoolean("cfg_gyroRawDataEnabled", config.isGyroRawDataEnabled());
        editor.putBoolean("cfg_gyroRawXDataEnabled", config.isGyroRawXDataEnabled());
        editor.putBoolean("cfg_gyroRawYDataEnabled", config.isGyroRawYDataEnabled());
        editor.putBoolean("cfg_gyroRawZDataEnabled", config.isGyroRawZDataEnabled());
        editor.putBoolean("cfg_gyroXDataFilterEnabled", config.isGyroXDataFilterEnabled());
        editor.putBoolean("cfg_gyroYDataFilterEnabled", config.isGyroYDataFilterEnabled());
        editor.putBoolean("cfg_gyroZDataFilterEnabled", config.isGyroZDataFilterEnabled());
        editor.putFloat("cfg_gyroXFilteredThreshold", config.getGyroXFilteredThreshold());
        editor.putFloat("cfg_gyroYFilteredThreshold", config.getGyroYFilteredThreshold());
        editor.putFloat("cfg_gyroZFilteredThreshold", config.getGyroZFilteredThreshold());

        editor.apply();
    }

    static public boolean getPreference(Context context, String name, boolean defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);

        return preferences.getBoolean(name, defaultValue);
    }

    static public float getPreference(Context context, String name, float defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);

        return preferences.getFloat(name, defaultValue);
    }

    static public int getPreference(Context context, String name, int defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);

        return preferences.getInt(name, defaultValue);
    }

    static public String getPreference(Context context, String name, String defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);

        return preferences.getString(name, defaultValue);
    }

    static public void restoreDeviceConfiguration(Context context, Configuration config) {
        Configuration defaultCfg = new Configuration();

        SharedPreferences preferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);

        config.setDataInterval(preferences.getInt("cfg_dataInterval", defaultCfg.getDataInterval()));
        config.setTemperatureDataEnabled(preferences.getBoolean("cfg_temperatureDataEnabled", defaultCfg.isTemperatureDataEnabled()));
        config.setEmulationEnabled(preferences.getBoolean("cfg_emulationEnabled", defaultCfg.isEmulationEnabled()));

        config.setLeftCrankDataEnabled(preferences.getBoolean("cfg_leftCrankDataEnabled", defaultCfg.isLeftCrankDataEnabled()));
        config.setRightCrankDataEnabled(preferences.getBoolean("cfg_rightCrankDataEnabled", defaultCfg.isRightCrankDataEnabled()));
        config.setCrankDataFilterEnabled(preferences.getBoolean("cfg_crankDataFilterEnabled", defaultCfg.isCrankDataFilterEnabled()));
        config.setCrankRawThreshold(preferences.getFloat("cfg_crankRawThreshold", defaultCfg.getCrankRawThreshold()));
        config.setCrankFilteredThreshold(preferences.getFloat("cfg_crankFilteredThreshold", defaultCfg.getCrankFilteredThreshold()));

        config.setAccXDataEnabled(preferences.getBoolean("cfg_accXDataEnabled", defaultCfg.isAccXDataEnabled()));
        config.setAccYDataEnabled(preferences.getBoolean("cfg_accYDataEnabled", defaultCfg.isAccYDataEnabled()));
        config.setAccZDataEnabled(preferences.getBoolean("cfg_accZDataEnabled", defaultCfg.isAccZDataEnabled()));
        config.setAccRawDataEnabled(preferences.getBoolean("cfg_accRawDataEnabled", defaultCfg.isAccRawDataEnabled()));
        config.setAccRawXDataEnabled(preferences.getBoolean("cfg_accRawXDataEnabled", defaultCfg.isAccRawXDataEnabled()));
        config.setAccRawYDataEnabled(preferences.getBoolean("cfg_accRawYDataEnabled", defaultCfg.isAccRawYDataEnabled()));
        config.setAccRawZDataEnabled(preferences.getBoolean("cfg_accRawZDataEnabled", defaultCfg.isAccRawZDataEnabled()));
        config.setAccXDataFilterEnabled(preferences.getBoolean("cfg_accXDataFilterEnabled", defaultCfg.isAccXDataFilterEnabled()));
        config.setAccYDataFilterEnabled(preferences.getBoolean("cfg_accYDataFilterEnabled", defaultCfg.isAccYDataFilterEnabled()));
        config.setAccZDataFilterEnabled(preferences.getBoolean("cfg_accZDataFilterEnabled", defaultCfg.isAccZDataFilterEnabled()));
        config.setAccXFilteredThreshold(preferences.getFloat("cfg_accXFilteredThreshold", defaultCfg.getAccXFilteredThreshold()));
        config.setAccYFilteredThreshold(preferences.getFloat("cfg_accYFilteredThreshold", defaultCfg.getAccYFilteredThreshold()));
        config.setAccZFilteredThreshold(preferences.getFloat("cfg_accZFilteredThreshold", defaultCfg.getAccZFilteredThreshold()));

        config.setGyroXDataEnabled(preferences.getBoolean("cfg_gyroXDataEnabled", defaultCfg.isGyroXDataEnabled()));
        config.setGyroYDataEnabled(preferences.getBoolean("cfg_gyroYDataEnabled", defaultCfg.isGyroYDataEnabled()));
        config.setGyroZDataEnabled(preferences.getBoolean("cfg_gyroZDataEnabled", defaultCfg.isGyroZDataEnabled()));
        config.setGyroRawDataEnabled(preferences.getBoolean("cfg_gyroRawDataEnabled", defaultCfg.isGyroRawDataEnabled()));
        config.setGyroRawXDataEnabled(preferences.getBoolean("cfg_gyroRawXDataEnabled", defaultCfg.isGyroRawXDataEnabled()));
        config.setGyroRawYDataEnabled(preferences.getBoolean("cfg_gyroRawYDataEnabled", defaultCfg.isGyroRawYDataEnabled()));
        config.setGyroRawZDataEnabled(preferences.getBoolean("cfg_gyroRawZDataEnabled", defaultCfg.isGyroRawZDataEnabled()));
        config.setGyroXDataFilterEnabled(preferences.getBoolean("cfg_gyroXDataFilterEnabled", defaultCfg.isGyroXDataFilterEnabled()));
        config.setGyroYDataFilterEnabled(preferences.getBoolean("cfg_gyroYDataFilterEnabled", defaultCfg.isGyroYDataFilterEnabled()));
        config.setGyroZDataFilterEnabled(preferences.getBoolean("cfg_gyroZDataFilterEnabled", defaultCfg.isGyroZDataFilterEnabled()));
        config.setGyroXFilteredThreshold(preferences.getFloat("cfg_gyroXFilteredThreshold", defaultCfg.getGyroXFilteredThreshold()));
        config.setGyroYFilteredThreshold(preferences.getFloat("cfg_gyroYFilteredThreshold", defaultCfg.getGyroYFilteredThreshold()));
        config.setGyroZFilteredThreshold(preferences.getFloat("cfg_gyroZFilteredThreshold", defaultCfg.getGyroZFilteredThreshold()));

    }
}
