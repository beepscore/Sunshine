package com.beepscore.android.sunshine;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * Created by stevebaker on 8/20/15.
 */
public class DisplayUtils {

    /**
     * http://stackoverflow.com/questions/9279111/determine-if-the-device-is-a-smartphone-or-tablet/9308284#9308284
     *
     * @param activity
     * @param diagonalThresholdInches display physical size threshold
     * e.g. can use 6.5 for a small tablet
     * @return true if display size is greater than or equal to diagonalThresholdInches
     */
    static boolean isDisplayLarge(Activity activity, double diagonalThresholdInches) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float yInches = metrics.heightPixels / metrics.ydpi;
        float xInches = metrics.widthPixels / metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
        if (diagonalInches >= diagonalThresholdInches) {
            return true;
        } else {
            return false;
        }
    }
}
