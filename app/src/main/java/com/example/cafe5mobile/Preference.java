package com.example.cafe5mobile;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

public final class Preference {

    private static final String prefName  = "com.nyt.taxi";

    private static SharedPreferences pref(Context context) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    public static boolean getBoolean(String key) {
        return pref(App.getContext()).getBoolean(key, false);
    }

    public static void setBoolean(String key, boolean value) {
        SharedPreferences.Editor e = pref(App.getContext()).edit();
        e.putBoolean(key, value);
        e.commit();
    }

    public static float getFloat(String key) {
        return pref(App.getContext()).getFloat(key, 0);
    }

    public static void setFloat(String key, float value) {
        SharedPreferences.Editor e = pref(App.getContext()).edit();
        e.putFloat(key, value);
        e.commit();
    }

    public static long getLong(String key) {
        return pref(App.getContext()).getLong(key, 0);
    }

    public static void setLong(String key, long value) {
        SharedPreferences.Editor e = pref(App.getContext()).edit();
        e.putLong(key, value);
        e.commit();
    }

    public static int getInt(String key) {
        return pref(App.getContext()).getInt(key, 0);
    }

    public static void setInt(String key, int value) {
        SharedPreferences.Editor e = pref(App.getContext()).edit();
        e.putInt(key, value);
        e.commit();
    }

    public static String getString(String key) {
        return pref(App.getContext()).getString(key, "");
    }

    public static void setString(String key, String value) {
        SharedPreferences.Editor e = pref(App.getContext()).edit();
        e.putString(key, value);
        e.commit();
    }

    public static String time() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) App.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
