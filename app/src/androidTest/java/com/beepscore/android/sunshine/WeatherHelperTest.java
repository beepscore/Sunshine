package com.beepscore.android.sunshine;

import android.app.Application;
import android.net.Uri;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class WeatherHelperTest extends ApplicationTestCase<Application> {
    public WeatherHelperTest() {
        super(Application.class);
    }

    public void testWeatherUri() {
        Uri expected = Uri.parse("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
        Uri actual = WeatherHelper.weatherUri("94043", "json", "metric", 7);
        assertEquals(expected, actual);
    }

}

