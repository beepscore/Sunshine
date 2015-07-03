package com.beepscore.android.sunshine;

import android.app.Application;
import android.net.Uri;
import android.test.ApplicationTestCase;

/**
 * Created by stevebaker on 7/3/15.
 */
public class MainActivityTest extends ApplicationTestCase<Application> {
    public MainActivityTest() {
        super(Application.class);
    }

    public void testGetGeoLocation() {
        MainActivity mainActivity = new MainActivity();

        assertEquals(Uri.parse("geo:?q=boise%2Cus"), mainActivity.getGeoLocation("boise,us"));
        assertEquals(Uri.parse("geo:?q=boise%2C%20us"), mainActivity.getGeoLocation("boise, us"));
        assertEquals(Uri.parse("geo:?q=st%20petersburg%2C%20us"), mainActivity.getGeoLocation("st petersburg, us"));
    }

}
