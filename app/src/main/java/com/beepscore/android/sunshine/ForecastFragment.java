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
import android.widget.ListView;
import android.widget.TextView;

import com.beepscore.android.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int LOADER_ID = 1;

    // public empty constructor
    public ForecastFragment() {
    }

    ForecastAdapter mForecastAdapter = null;

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
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);

        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.
        mForecastAdapter = new ForecastAdapter(getActivity(), cur, 0);

        View fragmentForecastView = inflater.inflate(R.layout.fragment_forecast, container, false);
        ListView listView = (ListView)fragmentForecastView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

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
            int numberOfRowsDeleted = getActivity().getContentResolver().delete(weatherUri,
                    null, null);

            // get new forecasts
            updateWeather();

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

        // e.g. "content://com.beepscore.android.sunshine/weather"
        Uri baseUri = WeatherContract.WeatherEntry.CONTENT_URI;

        return new CursorLoader(getActivity(),
                baseUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        TextView textView = (TextView)getActivity().findViewById(R.id.list_item_forecast_textview);

        if (mForecastAdapter != null
                && data != null
                && data.moveToFirst()
                && textView != null) {

            // Swap the new cursor in.
            // The framework will take care of closing the old cursor once we return.
            mForecastAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // called when the last Cursor provided to onLoadFinished() is about to be closed.
        // We need to make sure we are no longer using it.
        mForecastAdapter.swapCursor(null);
    }

    ////////////////////////////////////////////////////////////////////////////

}
