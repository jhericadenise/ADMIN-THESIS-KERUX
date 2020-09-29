package com.kerux.admin_thesis_kerux.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class KeruxSession {
    private SharedPreferences prefs;

    public KeruxSession(Context cntx) {
        // TODO Auto-generated constructor stub
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void setusername(String username) {
        prefs.edit().putString("username", username).commit();
    }

    public String getusername() {
        String username = prefs.getString("username","");
        return username;
    }
}
