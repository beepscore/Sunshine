package com.beepscore.android.sunshine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.beepscore.android.sunshine.data.WeatherContract;
import com.beepscore.android.sunshine.sync.SunshineSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    /**
     * A callback interface that all activities containing this fragment must implement.
     * This mechanism allows activities to be notified of item selections.
     * This mechanism enables passing information from this fragment to
     * the activity that has implemented the Callback interface.
     * This decouples ForecastFragment from a particular activity (e.g. MainActivity)
     * This also decouples the fragment from other fragments (e.g. DetailFragment)
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    /*
     * projection of columns we want to get from database
     */
    public static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };

    // These indices are tied to FORECAST_COLUMNS.
    // If FORECAST_COLUMNS changes, these must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final int COL_HUMIDITY = 9;
    static final int COL_PRESSURE = 10;
    static final int COL_WIND_SPEED = 11;
    static final int COL_DEGREES = 12;

    private final static int LOADER_ID = 1;
    private ForecastAdapter mForecastAdapter = null;
    private ListView mListView = null;
    private String dayForecast = "";

    static final String SELECTED_KEY = "POSITION";
    protected int mPosition = 0;

    private boolean mUseTodayLayout;

    // public empty constructor
    public ForecastFragment() {
    }

    @Override
    // onCreate is called before onCreateView
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Prepare the loader.
        // Either re-connect with an existing one or start a new one.
        // http://developer.android.com/guide/components/loaders.html
        // http://android-developer-tutorials.blogspot.com/2013/03/using-cursorloader-in-android.html
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // cursor isn't ready yet, so use null
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        View fragmentForecastView = inflater.inflate(R.layout.fragment_forecast, container, false);
        ListView listView = (ListView)fragmentForecastView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        mListView = listView;

        if ((savedInstanceState != null)
                && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {

                mPosition = position;

                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {

                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    long dateLong = cursor.getLong(COL_WEATHER_DATE);
                    Uri weatherLocationDateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, dateLong);

                    // Use callback to pass information from ForecastFragment
                    // to Callback implementing Activity
                    ((Callback)getActivity()).onItemSelected(weatherLocationDateUri);
                }
            }
        });

    return fragmentForecastView;
}

    // Lesson 4c says remove onStart to reduce excessive weather fetching.
    // Lesson 6 will schedule updates in the background.
    // Without onStart, on app first launch after install,
    // app doesn't updateWeather until user chooses menu Refresh.
    // On subsequent launches, app displays weather.
//    @Override
//    public void onStart() {
//        super.onStart();
//        updateWeather();
//    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(SELECTED_KEY, mPosition);
    }

    ///////////////////////////////////////////////////////////////////////////

    void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    ///////////////////////////////////////////////////////////////////////////

    private void openPreferredLocationInMap() {
        String locationPreference = PreferenceHelper.getLocationPreferenceString(getActivity());
        Uri geoLocation = getGeoLocation(locationPreference);
        showMapForUri(geoLocation);
    }

    /**
     * @param locationPreference is like "94043" or "seattle"
     * return geoLocation like geo:?q=94043 or geo:?q=seattle
     */
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
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private PendingIntent getPendingIntentForExplicitIntent(Intent intent) {
        // http://developer.android.com/reference/android/app/PendingIntent.html
        // http://stackoverflow.com/questions/3146883/combine-two-intent-flags
        // wont use request code, don't care about value
        int REQUEST_CODE_DONT_CARE = 0;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),
                REQUEST_CODE_DONT_CARE,
                intent,
                PendingIntent.FLAG_ONE_SHOT);
        return pendingIntent;
    }

    private void configureAlarm(PendingIntent pendingIntent) {
        // https://developer.android.com/training/scheduling/alarms.html#set
        AlarmManager alarmMgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        int FIVE_SEC_MSEC = 5 * 1000;
        Log.d(LOG_TAG, "configureAlarm");
        alarmMgr.set(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + FIVE_SEC_MSEC,
                pendingIntent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // http://stackoverflow.com/questions/15653737/oncreateoptionsmenu-inside-fragments
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {

            // delete old forecasts
            //Uri weatherUri = WeatherContract.WeatherEntry.CONTENT_URI;
            //int numberOfRowsDeleted = getActivity().getContentResolver().delete(weatherUri, null, null);

            // Note console log shows warning
            // Attempted to finish an input event but the input event receiver has already been disposed.

            // get new forecasts
            updateWeather();
            return true;
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////////////////
    // LoaderManager.LoaderCallbacks<Cursor>
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Called when a new Loader needs to be created.
        // This sample only has one Loader, so we don't care about the ID.

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        return new CursorLoader(getActivity(),
                getWeatherForLocationUri(),
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    private Uri getWeatherForLocationUri() {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        return WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.
        // The framework will take care of closing the old cursor once we return.
        mForecastAdapter.swapCursor(cursor);
        mListView.setSelection(mPosition);
        dayForecast = getDayForecast(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // called when the last Cursor provided to onLoadFinished() is about to be closed.
        // We need to make sure we are no longer using it.
        mForecastAdapter.swapCursor(null);
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * @param cursor
     * @return weather in format for sharing. e.g. "Tue 6/24 - Foggy - 21/8"
     */
    public static String getDayForecast(Cursor cursor) {
        String dayForecastString = "";
        if (cursor != null
                && cursor.moveToFirst()) {
            String separator = " - ";

            dayForecastString = Utility.formatDate(cursor.getLong(COL_WEATHER_DATE))
                    + separator
                    + cursor.getString(COL_WEATHER_DESC)
                    + separator
                    + cursor.getInt(COL_WEATHER_MAX_TEMP)
                    + "/"
                    + cursor.getInt(COL_WEATHER_MIN_TEMP);
        }
        return dayForecastString;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {

        mUseTodayLayout = useTodayLayout;

        // Note: this setter method has a side effect!
        // Pass call to mForecastAdapter
        // Don't expose private mForecastAdapter to other classes.

        // This is a public method,
        // and another class could call it before mForecastAdapter is initialized.
        // So use a guard clause
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

}
