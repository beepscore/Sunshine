package com.beepscore.android.sunshine;

import android.net.Uri;

/**
 * Created by stevebaker on 6/22/15.
 */
public class WeatherHelper {

    /**
     * URI for weather forecast query from OpenWeatherMap
     * @param postalCode specifies a geographic location
     * @param format web service desired result format e.g. json, xml
     * @param units web service desired result units e.g. metric
     * @param numberOfDays e.g. 7
     * @return uri
     * http://openweathermap.org/API#forecast
     */
    protected static Uri weatherUri(String postalCode,
                                    String format, String units, Integer numberOfDays) {

        final String SCHEME = "http";
        final String BASE_URL = "api.openweathermap.org";
        final String DATA = "data";
        final String API_VERSION = "2.5";
        final String FORECAST = "forecast";
        final String DAILY = "daily";
        final String QUERY_PARAM = "q";
        final String MODE_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String COUNT_PARAM = "cnt";

        // http://stackoverflow.com/questions/19167954/use-uri-builder-in-android-or-create-url-with-variables
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(BASE_URL)
                .appendPath(DATA)
                .appendPath(API_VERSION)
                .appendPath(FORECAST)
                .appendPath(DAILY)
                .appendQueryParameter(QUERY_PARAM, postalCode)
                .appendQueryParameter(MODE_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(COUNT_PARAM, Integer.toString(numberOfDays));

        Uri uri = builder.build();
        return uri;
    }

}
