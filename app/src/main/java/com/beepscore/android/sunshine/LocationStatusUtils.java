package com.beepscore.android.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by stevebaker on 10/28/15.
 */
public class LocationStatusUtils {

    // https://sites.google.com/a/android.com/tools/tech-docs/support-annotations
    // http://developer.android.com/reference/android/support/annotation/IntDef.html
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOCATION_STATUS_OK, LOCATION_STATUS_SERVER_DOWN, LOCATION_STATUS_SERVER_INVALID, LOCATION_STATUS_UNKNOWN})

    public @interface LocationStatus {}
    public static final int LOCATION_STATUS_OK = 0;
    public static final int LOCATION_STATUS_SERVER_DOWN = 1;
    public static final int LOCATION_STATUS_SERVER_INVALID = 2;
    public static final int LOCATION_STATUS_UNKNOWN = 3;
    public static final int LOCATION_STATUS_INVALID = 4;
    @LocationStatus

    public static int getLocationStatus(Context context) {
        // In SharedPreferences get value for key
        // http://stackoverflow.com/questions/2614719/how-do-i-get-the-sharedpreferences-from-a-preferenceactivity-in-android
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String locationStatusKey = context.getString(R.string.pref_location_status_key);
        // if no key-value pair for locationStatusKey, default to LOCATION_STATUS_UNKNOWN
        return preferences.getInt(locationStatusKey, LOCATION_STATUS_UNKNOWN);
    }

    /**
     * Sets the location status into shared preferences.
     * This should not be called from the UI thread
     * because it uses commit to write to the shared preferences.
     * @param context
     * @param locationStatus
     */
    public static void setLocationStatus(Context context, @LocationStatus int locationStatus) {
        // In SharedPreferences set key/value pair
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        String locationStatusKey = context.getString(R.string.pref_location_status_key);
        editor.putInt(locationStatusKey, locationStatus);

        // use commit() for background thread
        // for foreground thread use apply()
        editor.apply();
    }

    /**
     * Sets the location status into shared preferences as LOCATION_STATUS_UNKNOWN.
     * This should not be called from the UI thread
     * because it uses commit to write to the shared preferences.
     * @param context
     */
    public static void setLocationStatusUnknown(Context context) {
        LocationStatusUtils.setLocationStatus(context, LOCATION_STATUS_UNKNOWN);
    }

}
