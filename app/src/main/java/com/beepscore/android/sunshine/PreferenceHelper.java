package com.beepscore.android.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by stevebaker on 7/3/15.
 *
 * StackOverflow answer advises ok to pass context as method argument to another class
 * but don't store it.
 * Storing context can cause memory leak or disrupt lifecycle
 * http://stackoverflow.com/questions/7454373/call-getstringr-strings-from-class
 */
public class PreferenceHelper {

    protected static String getLocationPreferenceString(Context context) {
        // http://stackoverflow.com/questions/2614719/how-do-i-get-the-sharedpreferences-from-a-preferenceactivity-in-android
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String keyName = context.getString(R.string.pref_location_key);
        // if no key-value pair for keyName, use pref_location_default
        return preferences.getString(keyName,
                context.getString(R.string.pref_location_default));
    }

    protected static String getUnitsPreferenceString(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String keyName = context.getString(R.string.pref_units_key);
        // if no key-value pair for keyName, use pref_units_metric
        return preferences.getString(keyName,
                context.getString(R.string.pref_units_metric));
    }

    public static long getLastSyncPreference(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String keyName = context.getString(R.string.pref_last_notification);
        long lastSync = preferences.getLong(keyName, 0);
        return lastSync;
    }

    public static void setLastSyncPreference(Context context, long timeMsec) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        String keyName = context.getString(R.string.pref_last_notification);
        editor.putLong(keyName, timeMsec);
        editor.commit();
    }

    public static Boolean getIsNotificationPreference(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String keyName = context.getString(R.string.pref_weather_notification_key);
        // Use local variable for easier debugging inspection
        // if no key-value pair for keyName, use false
        Boolean isNotificationPreference = preferences.getBoolean(keyName, false);
        return isNotificationPreference;
    }

}
