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
     *
     * @return true if network has connectivity (i.e. is reachable), else return false
     */
    boolean hasConnectivity(Context context) {

        // http://developer.android.com/reference/android/net/ConnectivityManager.html
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // I tried using EXTRA_NO_CONNECTIVITY but didn't get it to work
        // boolean noConnectivityExtra = activity.getIntent().getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

        // http://stackoverflow.com/questions/11062840/best-way-to-check-for-internet-connection-in-android?rq=1
        if (connectivityManager != null) {
            Network[] networks = connectivityManager.getAllNetworks();
            if ((networks != null)
                    && (networks.length > 0)) {
                for (Network network : networks) {
                    NetworkInfo info = connectivityManager.getNetworkInfo(network);
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
