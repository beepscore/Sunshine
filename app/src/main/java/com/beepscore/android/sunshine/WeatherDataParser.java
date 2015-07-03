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

    // Names of the JSON objects to extract
    final static String OWM_LIST = "list";
    final static String OWM_WEATHER = "weather";
    final static String OWM_TEMPERATURE = "temp";
    final static String OWM_MAX = "max";
    final static String OWM_MIN = "min";
    final static String OWM_DESCRIPTION = "main";
    final static String OWM_HUMIDITY = "humidity";

    /**
     * @param weatherJsonStr a string of the form returned by the api call
     * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
     * @param dayIndex Note: 0-indexed, so 0 refers to the first day.
     * @return the maximum temperature for the day indicated by dayIndex
     */
    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {

        // http://stackoverflow.com/questions/9605913/how-to-parse-json-in-android
        JSONArray weatherDays = getWeatherDaysFromJsonString(weatherJsonStr);
        JSONObject weatherDay = weatherDays.getJSONObject(dayIndex);
        double max = getHigh(weatherDay);
        return max;
    }

    protected static JSONArray getWeatherDaysFromJsonString(String forecastJsonStr)
            throws JSONException {
        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherDays = forecastJson.getJSONArray(OWM_LIST);
        return weatherDays;
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private static String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
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

        JSONArray weatherDays = getWeatherDaysFromJsonString(forecastJsonStr);

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherDays.length(); i++) {

            JSONObject weatherDay = weatherDays.getJSONObject(i);

            String day = getDayString(i);
            String description = getDescription(weatherDay);

            double high = getHigh(weatherDay);
            double low = getLow(weatherDay);
            String highAndLow = formatHighLows(high, low);

            // format "Day - description - hi/low"
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }
        return resultStrs;
    }

    protected static String getDayString(int i) {

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        // TODO: Consider replace deprecated class Time
        Time timeNow = new Time();
        timeNow.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), timeNow.gmtoff);

        // now we work exclusively in UTC
        Time dayTime = new Time();
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

    /**
     * @param weatherDay a json object representing the weather for one day
     * @return the description e.g. Clear, Clouds, Rain
     */
    protected static String getDescription(JSONObject weatherDay) throws JSONException {
        String description;// description is in a child array called "weather", which is 1 element long.
        JSONObject weatherObject = weatherDay.getJSONArray(OWM_WEATHER).getJSONObject(0);
        description = weatherObject.getString(OWM_DESCRIPTION);
        return description;
    }

    /**
     * @param weatherDay a json object representing the weather for one day
     * @return the maximum temperature
     */
    protected static double getHigh(JSONObject weatherDay) throws JSONException {
        JSONObject temperatureObject = getTemperatureJson(weatherDay);
        return temperatureObject.getDouble(OWM_MAX);
    }

    /**
     * @param weatherDay a json object representing the weather for one day
     * @return the minimum temperature
     */
    protected static double getLow(JSONObject weatherDay) throws JSONException {
        JSONObject temperatureObject = getTemperatureJson(weatherDay);
        return temperatureObject.getDouble(OWM_MIN);
    }

    private static JSONObject getTemperatureJson(JSONObject weatherDay) throws JSONException {
        // Temperatures are in a child object called "temp".
        // Try not to name variables "temp" when working with temperature.
        // It confuses everybody.
        return weatherDay.getJSONObject(OWM_TEMPERATURE);
    }

    /**
     * @param weatherDay a json object representing the weather for one day
     * @return the humidity as an integer from 0-100 inclusive
     */
    protected static Integer getHumidity(JSONObject weatherDay) throws JSONException {
        Integer humidity = weatherDay.getInt(OWM_HUMIDITY);
        return humidity;
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

}
