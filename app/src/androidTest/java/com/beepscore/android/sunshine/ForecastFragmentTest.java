package com.beepscore.android.sunshine;

import android.app.Application;
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

}
