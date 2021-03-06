package com.beepscore.android.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.beepscore.android.sunshine.gcm.RegistrationIntentService;
import com.beepscore.android.sunshine.sync.SunshineSyncAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class MainActivity extends AppCompatActivity
        implements ForecastFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";

    protected boolean mTwoPane;
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

        ForecastFragment forecastFragment = (ForecastFragment)(getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));

        // on one pane (e.g. phone), use today layout
        // on two panes (e.g. tablet), don't use today layout
        forecastFragment.setUseTodayLayout(!mTwoPane);

        if (mTwoPane == false) {
            // remove shadow below aciton bar
            getSupportActionBar().setElevation(0);
        }

        SunshineSyncAdapter.initializeSyncAdapter(this);

        // If Google Play Services is up to date, we'll want to register GCM. If it is not, we'll
        // skip the registration and this device will not receive any downstream messages from
        // our fake server. Because weather alerts are not a core feature of the app, this should
        // not affect the behavior of the app, from a user perspective.
        if (checkPlayServices()) {
            // Because this is the initial creation of the app, we'll want to be certain we have
            // a token. If we do not, then we will start the IntentService that will register this
            // application with GCM.
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            boolean sentToken = sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false);
            if (!sentToken) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, " onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, " onResume");

        // Android calls onResume after resuming from onPause
        // if user taps Settings, that pauses MainActivity
        // User might set a new location
        // Then when user leaves Settings, Android calls onResume
        // Check if location has changed.
        // If it has, call fragment onLocationChanged methods so they can update themselves

        String location = PreferenceHelper.getLocationPreferenceString(this);
        if (location != null
                && !location.equals(mLocation)) {
            // location has changed, update it
            mLocation = location;

            // update forecastFragment
            ForecastFragment forecastFragment = (ForecastFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_forecast);
            if (forecastFragment != null) {
                forecastFragment.onLocationChanged();
            }

            // if app is showing two panes, detailFragment is present. Update detailFragment
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(location);
            }

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
        //int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    ///////////////////////////////////////////////////////////////////////////

    // ForecastFragment.Callback
    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // Two-pane mode, in this activity show DetailFragment view

            // Every fragment has a property named arguments of type Bundle.
            // This Bundle is separate from the savedInstanceState Bundle.
            // Use fragment.setArguments to pass info from the activity to the fragment

            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            // Use fragment transaction to replace the detail fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();


        } else {
            // single pane mode, start DetailActivity to show DetailFragment view
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
