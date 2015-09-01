package com.beepscore.android.sunshine.service;

import android.app.IntentService;
import android.content.Intent;

import com.beepscore.android.sunshine.FetchWeatherTask;
import com.beepscore.android.sunshine.R;

/**
 * Created by stevebaker on 8/31/15.
 */
public class SunshineService extends IntentService {

    public SunshineService() {
        super("SunshineService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // If there's no zip code, there's nothing to look up, so exit.
        String location = intent.getStringExtra(getString(R.string.location));
        if (location == null) {
            return;
        }

        FetchWeatherTask weatherTask = new FetchWeatherTask(this);
        weatherTask.execute(location);
    }

}
