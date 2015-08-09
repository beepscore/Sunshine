package com.beepscore.android.sunshine;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class UtilityTest extends ApplicationTestCase<Application> {
    public UtilityTest() {
        super(Application.class);
    }

    public void testDegreesCToDegreesF() {
        assertEquals(32.0, Utility.degreesCToDegreesF(0), 0.001);
        assertEquals(212.0, Utility.degreesCToDegreesF(100), 0.001);
        assertEquals(59.0, Utility.degreesCToDegreesF(15), 0.001);
    }
}

