package com.jin.fidoclient.utils;

import android.content.SharedPreferences;

import com.jin.fidoclient.api.UAFClientApi;

public class Preferences {

    private static String PREFERENCES = "Preferences";

    public static String getSettingsParam(String paramName) {
        SharedPreferences settings = getPreferences();
        return settings.getString(paramName, "");
    }

    public static SharedPreferences getPreferences() {
        SharedPreferences settings = UAFClientApi.getContext().getSharedPreferences(PREFERENCES, 0);
        return settings;
    }

    public static void setSettingsParam(String paramName, String paramValue) {
        SharedPreferences settings = getPreferences();
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(paramName, paramValue);
        editor.commit();
    }

    public static void setSettingsParamLong(String paramName, long paramValue) {
        SharedPreferences settings = getPreferences();
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(paramName, paramValue);
        editor.commit();
    }

}
