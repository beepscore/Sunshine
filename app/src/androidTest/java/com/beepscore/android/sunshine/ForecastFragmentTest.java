package com.beepscore.android.sunshine;

import android.app.Application;
import android.net.Uri;
import android.test.ApplicationTestCase;

import java.text.NumberFormat;

/**
 * Created by stevebaker on 7/3/15.
 */
public class ForecastFragmentTest extends ApplicationTestCase<Application> {
    public ForecastFragmentTest() {
        super(Application.class);
    }

    /**
     * Test app uses NumberFormat correctly.
     * Not designed as a test of Android or Java framework methods.
     */
    public void testNumberFormat() {
        // getIntegerInstance rounds, doesn't truncate.
        NumberFormat formatter = NumberFormat.getIntegerInstance();
        assertEquals("0", formatter.format(0.49));
        assertEquals("1", formatter.format(0.51));
        assertEquals("25", formatter.format(24.51));
    }

    public void testGetGeoLocation() {
        ForecastFragment forecastFragment = new ForecastFragment();

        assertEquals(Uri.parse("geo:?q=boise%2Cus"), forecastFragment.getGeoLocation("boise,us"));
        assertEquals(Uri.parse("geo:?q=boise%2C%20us"), forecastFragment.getGeoLocation("boise, us"));
        assertEquals(Uri.parse("geo:?q=st%20petersburg%2C%20us"), forecastFragment.getGeoLocation("st petersburg, us"));
    }

    public void testGetGeoLocationForLatLon() {
        ForecastFragment forecastFragment = new ForecastFragment();

        assertEquals(Uri.parse("geo:1.2,-34.5"), forecastFragment.getGeoLocationForLatLon("1.2", "-34.5"));

        // for Seattle, latitude is like "47.6062", longitude is like "-122.332"
        assertEquals(Uri.parse("geo:47.6062,-122.332"), forecastFragment.getGeoLocationForLatLon("47.6062", "-122.332"));
    }
}
