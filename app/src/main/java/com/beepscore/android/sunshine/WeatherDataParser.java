package com.beepscore.android.sunshine;

import android.text.format.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Created by stevebaker on 6/22/15.
 * Based on Udacity Lesson 2 JSON parsing quiz
 */
public class WeatherDataParser {

    // Names of the JSON objects that need to be extracted.
    final String OWM_LIST = "list";
    final String OWM_WEATHER = "weather";
    final String OWM_TEMPERATURE = "temp";
    final String OWM_MAX = "max";
    final String OWM_MIN = "min";
    final String OWM_DESCRIPTION = "main";

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

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    protected String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        JSONArray weatherArray = getJsonDaysFromJsonString(forecastJsonStr);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        // TODO: Consider replace deprecated class Time
        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {

            // For now, using the format "Day, description, hi/low"
            String highAndLow;

            // JSON object representing day i
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            String day = getDayString(dayTime, julianStartDay, i);
            String description = getDescription(dayForecast);

            double high = getHigh(dayForecast);
            double low = getLow(dayForecast);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        return resultStrs;
    }
    protected JSONArray getJsonDaysFromJsonString(String forecastJsonStr)
            throws JSONException {
        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray daysArray = forecastJson.getJSONArray(OWM_LIST);
        return daysArray;
    }

    private String getDayString(Time dayTime, int julianStartDay, int i) {
        String day;
        // The date/time is returned as a long.  We need to convert that
        // into something human-readable, since most people won't read "1400356800" as
        // "this saturday".
        long dateTime;

        // Cheating to convert this to UTC time, which is what we want anyhow
        dateTime = dayTime.setJulianDay(julianStartDay+i);
        day = getReadableDateString(dateTime);

        return day;
    }

    private String getDescription(JSONObject dayForecast) throws JSONException {
        String description;// description is in a child array called "weather", which is 1 element long.
        JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
        description = weatherObject.getString(OWM_DESCRIPTION);
        return description;
    }

    private double getHigh(JSONObject dayForecast) throws JSONException {
        JSONObject temperatureObject = getTemperatureJson(dayForecast);
        return temperatureObject.getDouble(OWM_MAX);
    }

    private double getLow(JSONObject dayForecast) throws JSONException {
        JSONObject temperatureObject = getTemperatureJson(dayForecast);
        return temperatureObject.getDouble(OWM_MIN);
    }

    private JSONObject getTemperatureJson(JSONObject dayForecast) throws JSONException {
        // Temperatures are in a child object called "temp".
        // Try not to name variables "temp" when working with temperature.
        // It confuses everybody.
        return dayForecast.getJSONObject(OWM_TEMPERATURE);
    }

}
