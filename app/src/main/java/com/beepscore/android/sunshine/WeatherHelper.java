package com.beepscore.android.sunshine;

import android.net.Uri;

/**
 * Created by stevebaker on 6/22/15.
 */
public class WeatherHelper {

    /**
     * URI for weather forecast query from OpenWeatherMap
     * @param postalCode string for a query parameter
     * @return uri
     * http://openweathermap.org/API#forecast
     */
    protected static Uri weatherUri(String postalCode) {

        // http://stackoverflow.com/questions/19167954/use-uri-builder-in-android-or-create-url-with-variables
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("forecast")
                .appendPath("daily")
                .appendQueryParameter("q", postalCode)
                .appendQueryParameter("mode", "json")
                .appendQueryParameter("units", "metric")
                .appendQueryParameter("cnt", "7");

        Uri uri = builder.build();
        return uri;
    }

}
