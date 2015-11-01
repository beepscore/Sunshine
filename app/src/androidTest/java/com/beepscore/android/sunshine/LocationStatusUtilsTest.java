package com.beepscore.android.sunshine;

import android.app.Application;
import android.test.ApplicationTestCase;


/**
 * Created by stevebaker on 10/30/15.
 */
public class LocationStatusUtilsTest extends ApplicationTestCase<Application> {
    public LocationStatusUtilsTest() {
        super(Application.class);
    }

    public void testLocationStatus() {
        int actual;
        int fakeStatus;

        fakeStatus = LocationStatusUtils.LOCATION_STATUS_OK;
        LocationStatusUtils.setLocationStatus(getContext(), fakeStatus);
        actual = LocationStatusUtils.getLocationStatus(getContext());
        assertEquals(fakeStatus, actual);

        fakeStatus = LocationStatusUtils.LOCATION_STATUS_SERVER_INVALID;
        LocationStatusUtils.setLocationStatus(getContext(), fakeStatus);
        actual = LocationStatusUtils.getLocationStatus(getContext());
        assertEquals(fakeStatus, actual);

        // clean up
        fakeStatus = LocationStatusUtils.LOCATION_STATUS_UNKNOWN;
        LocationStatusUtils.setLocationStatus(getContext(), fakeStatus);
        actual = LocationStatusUtils.getLocationStatus(getContext());
        assertEquals(fakeStatus, actual);
    }

    public void testLocationStatusUnknown() {
        int actual;
        int fakeStatus;

        // setup
        fakeStatus = LocationStatusUtils.LOCATION_STATUS_SERVER_INVALID;
        LocationStatusUtils.setLocationStatus(getContext(), fakeStatus);
        actual = LocationStatusUtils.getLocationStatus(getContext());
        assertEquals(fakeStatus, actual);

        // test method and clean up at the same time
        LocationStatusUtils.setLocationStatusUnknown(getContext());
        actual = LocationStatusUtils.getLocationStatus(getContext());
        assertEquals(LocationStatusUtils.LOCATION_STATUS_UNKNOWN, actual);
    }
}
