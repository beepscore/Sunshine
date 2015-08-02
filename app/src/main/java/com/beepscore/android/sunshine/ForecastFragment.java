package com.beepscore.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.beepscore.android.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

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
    private String dayForecast = "";

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

        View fragmentForecastView = inflater.inflate(R.layout.fragment_forecast, container, false);
        ListView listView = (ListView)fragmentForecastView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                    intent.putExtra(Intent.EXTRA_TEXT, dayForecast);
                    startActivity(intent);
                }
            }
        });

        return fragmentForecastView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // http://stackoverflow.com/questions/15653737/oncreateoptionsmenu-inside-fragments
        inflater.inflate(R.menu.forecastfragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {

            // delete old forecasts
            Uri weatherUri = WeatherContract.WeatherEntry.CONTENT_URI;
            int numberOfRowsDeleted = getActivity().getContentResolver().delete(weatherUri, null, null);

            // Note console log shows warning
            // Attempted to finish an input event but the input event receiver has already been disposed.

            // get new forecasts
            updateWeather();

            // Reference Lesson 4c Handle the Settings Change
            getLoaderManager().restartLoader(LOADER_ID, null, this);

            return true;
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
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

}
