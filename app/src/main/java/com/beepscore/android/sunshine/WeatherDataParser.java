package com.beepscore.android.sunshine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by stevebaker on 6/22/15.
 * Based on Udacity Lesson 2 JSON parsing quiz
 */
public class WeatherDataParser {

    /**
     * Given a string of the form returned by the api call:
     * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
     * retrieve the maximum temperature for the day indicated by dayIndex
     * (Note: 0-indexed, so 0 would refer to the first day).
     */
    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {

        // http://stackoverflow.com/questions/9605913/how-to-parse-json-in-android
        JSONObject weatherObj = new JSONObject(weatherJsonStr);

        JSONArray listArray = weatherObj.getJSONArray("list");
        JSONObject listElement = listArray.getJSONObject(dayIndex);
        JSONObject temp = listElement.getJSONObject("temp");
        double max = temp.getDouble("max");

        return max;
    }

}
