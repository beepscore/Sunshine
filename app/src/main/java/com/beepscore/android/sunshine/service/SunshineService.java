package com.beepscore.android.sunshine.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by stevebaker on 8/31/15.
 */
public class SunshineService extends IntentService {

    public SunshineService() {
        super("SunshineService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
