package com.beepscore.android.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;
    private String mLocation;

    ///////////////////////////////////////////////////////////////////////////
    // activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_main);
        mLocation = PreferenceHelper.getLocationPreferenceString(this);

        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, " onStart");
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, " onResume");
        super.onResume();

        String location = PreferenceHelper.getLocationPreferenceString(this);
        // update the location in our second pane using the fragment manager
        if (location != null
                && !location.equals(mLocation)) {
            ForecastFragment forecastFragment = (ForecastFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_forecast);
            if (forecastFragment != null) {
                forecastFragment.onLocationChanged();
            }
            mLocation = location;
        }
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, " onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, " onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, " onDestroy");
        super.onDestroy();
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {
        String locationPreference = PreferenceHelper.getLocationPreferenceString(this);
        Uri geoLocation = getGeoLocation(locationPreference);
        showMapForUri(geoLocation);
    }

    protected Uri getGeoLocation(String locationPreference) {
        // Uri builder escapes any spaces in locationPreference string to %20
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", locationPreference)
                .build();
        return geoLocation;
    }

    // https://developer.android.com/guide/components/intents-common.html
    private void showMapForUri(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        // if device doesn't have any apps to handle this intent, startActivity would crash
        // so check resolveActivity before attempting to startActivity
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}
