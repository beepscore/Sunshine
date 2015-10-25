package com.beepscore.android.sunshine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

/**
 * Created by stevebaker on 10/25/15.
 */
public class NetworkUtils {

    /**
     * @param context Context used to get the ConnectivityManager
     * @return true if network has connectivity (i.e. is reachable), else return false
     */
    static public boolean isNetworkAvailable(Context context) {

        // http://developer.android.com/reference/android/net/ConnectivityManager.html
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // I tried using EXTRA_NO_CONNECTIVITY but didn't get it to work
        // boolean noConnectivityExtra = activity.getIntent().getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return ((activeNetworkInfo != null)
                && activeNetworkInfo.isConnectedOrConnecting());
    }
}
