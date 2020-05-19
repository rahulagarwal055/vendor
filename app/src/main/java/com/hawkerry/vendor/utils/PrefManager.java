package com.hawkerry.vendor.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "hawkerry";

    public PrefManager(Context c) {
        sharedPreferences = c.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void setUUID(String uuid) {
        editor.putString("UUID", uuid);
        editor.apply();
        editor.commit();
    }

    public String getUUID() {
        return sharedPreferences.getString("UUID", null);
    }
}
