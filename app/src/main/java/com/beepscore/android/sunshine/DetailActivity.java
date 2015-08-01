package com.beepscore.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beepscore.android.sunshine.data.WeatherContract;

// References:
// https://github.com/udacity/Sunshine-Version-2/blob/3.02_create_detail_activity/app/src/main/java/com/example/android/sunshine/app/DetailActivity.java

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
        // http://www.androidhive.info/2013/11/android-working-with-action-bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        // show icon per specification. Discouraged on devices >= API 21
        // http://codetheory.in/difference-between-setdisplayhomeasupenabled-sethomebuttonenabled-and-setdisplayshowhomeenabled/
        actionBar.setIcon(R.drawable.ic_launcher);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////////////////
    public static class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";

        // ShareActionProvider shares weather as a String
        // e.g. "Tue 6/24 - Foggy - 21/8 #SunshineApp"
        private ShareActionProvider mShareActionProvider;
        private String locationSetting = "";
        private String weatherDate = "";
        private String weatherDesc = "";
        private int weatherMaxTemp;
        private int weatherMinTemp;

        // dayForecast used for sharing
        private String dayForecast = "";

        private Uri uri = null;
        private TextView textView = null;

        private final static int LOADER_ID = 2;

        public DetailFragment() {
            // setHasOptionsMenu to ensure options menu methods get called
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // get the intent the activity was started with
            // http://stackoverflow.com/questions/11387740/where-how-to-getintent-getextras-in-an-android-fragment
            Intent intent = getActivity().getIntent();

            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                this.textView = (TextView)rootView.findViewById(R.id.detail_text_view);
                this.textView.setText("coming soon");

                dayForecast = intent.getStringExtra(Intent.EXTRA_TEXT);
                uri = intent.getData();
                if (uri != null) {
                    // Use a CursorLoader (not CursorAdapter) to load data
                    // Prepare the loader.
                    // Either re-connect with an existing one or start a new one.
                    // http://developer.android.com/guide/components/loaders.html
                    // http://android-developer-tutorials.blogspot.com/2013/03/using-cursorloader-in-android.html
                    getLoaderManager().initLoader(LOADER_ID, null, this);
                }
            }

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            // http://stackoverflow.com/questions/15653737/oncreateoptionsmenu-inside-fragments
            inflater.inflate(R.menu.detailfragment, menu);

            MenuItem shareMenuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent
            mShareActionProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);

            // Attach an intent to this ShareActionProvider.
            // You can update this at any time, such as when
            // the user selects a new piece of data they might like to share.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent(dayForecast));
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null.");
            }

            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.action_settings) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            }

            if (id == R.id.action_share) {
                // do nothing. mShareActionProvider will handle click.
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private Intent createShareForecastIntent(String forecastString) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            // TODO: Consider replace deprecated FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, forecastString + FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }

        ////////////////////////////////////////////////////////////////////////////
        // LoaderManager.LoaderCallbacks<Cursor>
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // Called when a new Loader needs to be created.
            // This sample only has one Loader, so we don't care about the ID.

            // Sort order:  Ascending, by date.
            String sortOrder = ForecastFragment.COL_WEATHER_DATE + " ASC";

            return new CursorLoader(getActivity(),
                    this.uri,
                    ForecastFragment.FORECAST_COLUMNS,
                    null,
                    null,
                    sortOrder);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            // Swap the new cursor in.
            // The framework will take care of closing the old cursor once we return.
            String separator = " ";
            if (cursor != null
                    && cursor.moveToFirst()) {
                locationSetting = cursor.getString(ForecastFragment.COL_LOCATION_SETTING);
                weatherDesc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
                weatherDate = Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
                weatherMinTemp = cursor.getInt(ForecastFragment.COL_WEATHER_MIN_TEMP);
                weatherMaxTemp = cursor.getInt(ForecastFragment.COL_WEATHER_MAX_TEMP);

                String detailForecast = locationSetting
                        + separator + weatherDate
                        + separator + weatherMaxTemp
                        + "/" + weatherMinTemp
                        + separator + weatherDesc;

                this.textView.setText(detailForecast);

                if (mShareActionProvider != null) {

                    dayForecast = ForecastFragment.getDayForecast(cursor);

                    mShareActionProvider.setShareIntent(createShareForecastIntent(dayForecast));
                } else {
                    Log.d(LOG_TAG, "Share Action Provider is null.");
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // called when the last Cursor provided to onLoadFinished() is about to be closed.
            // We need to make sure we are no longer using it.
        }

        ////////////////////////////////////////////////////////////////////////////

    }
}