package com.beepscore.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beepscore.android.sunshine.data.WeatherContract;
import com.bumptech.glide.Glide;

/**
 * Created by stevebaker on 8/9/15.
 */
public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";

    // http://openweathermap.org/current#current_JSON
    // ShareActionProvider shares weather as a String
    // e.g. "Tue 6/24 - Foggy - 21/8 #SunshineApp"
    private ShareActionProvider mShareActionProvider;

    private String locationSetting = "";
    private String weatherDate = "";
    private String weatherDay = "";
    private String weatherDesc = "";
    private int weatherConditionId = -1;
    private double weatherHumidity;
    private double weatherTemperatureMax;
    private double weatherTemperatureMin;
    private double weatherPressure;

    // TODO get and parse wind speed and degrees
    // "wind":{"speed":5.1,"deg":150}
    private double weatherWindSpeed;
    private double weatherWindDegrees;

    // dayForecast used for sharing
    private String dayForecast = "";

    private Uri mUri = null;

    private ImageView descImageView = null;
    private TextView dayTextView = null;
    private TextView dateTextView = null;
    private TextView descTextView = null;
    private TextView humidityTextView = null;
    private TextView pressureTextView = null;
    private TextView temperatureMaxTextView = null;
    private TextView temperatureMinTextView = null;
    private TextView windTextView = null;
    private MyView windCompassView = null;

    private final static int LOADER_ID = 2;

    public DetailFragment() {
        // setHasOptionsMenu to ensure options menu methods get called
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The instantiating activity sets this fragment's arguments
        // as a way to pass information to it.
        // This decouples the fragment from a particular activity.
        // Note the Bundle from getArguments() is separate from Bundle savedInstanceState
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // get the intent the activity was started with
        // http://stackoverflow.com/questions/11387740/where-how-to-getintent-getextras-in-an-android-fragment
        Intent intent = getActivity().getIntent();

        if (intent != null) {
            dateTextView = (TextView) rootView.findViewById(R.id.date_text_view);
            dayTextView = (TextView) rootView.findViewById(R.id.day_text_view);
            descTextView = (TextView) rootView.findViewById(R.id.description_text_view);
            temperatureMaxTextView = (TextView) rootView.findViewById(R.id.list_item_high_text_view);
            temperatureMinTextView = (TextView) rootView.findViewById(R.id.list_item_low_text_view);
            humidityTextView = (TextView) rootView.findViewById(R.id.humidity_text_view);
            pressureTextView = (TextView) rootView.findViewById(R.id.pressure_text_view);
            windTextView = (TextView) rootView.findViewById(R.id.wind_text_view);
            windCompassView = (MyView) rootView.findViewById(R.id.wind_compass_view);
            descImageView = (ImageView) rootView.findViewById(R.id.description_image_view);

            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                dayForecast = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }

        // Use a CursorLoader (not CursorAdapter) to load data
        // Prepare the loader.
        // Either re-connect with an existing one or start a new one.
        // http://developer.android.com/guide/components/loaders.html
        // http://android-developer-tutorials.blogspot.com/2013/03/using-cursorloader-in-android.html
        getLoaderManager().initLoader(LOADER_ID, null, this);

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
        if (dayForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent(dayForecast));
        } else {
            Log.d(LOG_TAG, "dayForecast is null.");
        }
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
        Log.v(LOG_TAG, "In onCreateLoader");

        if ( null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.

            // Sort order:  Ascending, by date.
            String sortOrder = ForecastFragment.COL_WEATHER_DATE + " ASC";

            return new CursorLoader(
                    getActivity(),
                    mUri,
                    ForecastFragment.FORECAST_COLUMNS,
                    null,
                    null,
                    sortOrder
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.
        // The framework will take care of closing the old cursor once we return.
        String separator = " ";
        if (cursor != null
                && cursor.moveToFirst()) {
            updateWeatherProperties(getActivity(), cursor);
            updateUI();

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

    private void updateWeatherProperties(Context context, Cursor cursor) {
        locationSetting = cursor.getString(ForecastFragment.COL_LOCATION_SETTING);
        weatherDate = Utility.getFormattedMonthDay(context, cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
        weatherDay = Utility.getDayName(context, cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
        weatherDesc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        weatherConditionId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        weatherHumidity = cursor.getDouble(ForecastFragment.COL_HUMIDITY);
        weatherPressure = cursor.getDouble(ForecastFragment.COL_PRESSURE);
        weatherTemperatureMax = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        weatherTemperatureMin = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        weatherWindSpeed = cursor.getDouble(ForecastFragment.COL_WIND_SPEED);
        weatherWindDegrees = cursor.getDouble(ForecastFragment.COL_DEGREES);
    }

    //TODO bind adapter to views, then it will update views instead??
    private void updateUI() {
        if (weatherDay != null) {
            dayTextView.setText(weatherDay);
        }
        if (weatherDate != null) {
            dateTextView.setText(weatherDate);
        }
        if (weatherDesc != null) {
            descTextView.setText(weatherDesc);
            // Udacity lesson suggests explicitly setContentDescription null for decorative graphics.
            // This is more verbose than not setting it and doesn't seem to have any effect.
            // Why do this? To help document programmer intent?
            // http://stackoverflow.com/questions/9197661/contentdescription-null-a-bad-idea
            descImageView.setContentDescription(null);
        }

        if (weatherConditionId != -1) {
            Glide.with(this)
                    .load(WeatherHelper.getArtUrlForWeatherCondition(getActivity(), weatherConditionId))
                            .error(WeatherHelper.getArtResourceForWeatherCondition(weatherConditionId))
                            .into(descImageView);
        }

        String temperatureMax = Utility.formatTemperature(getActivity(),
                weatherTemperatureMax,
                Utility.isMetric(getActivity()));
        temperatureMaxTextView.setText(temperatureMax);
        // accessibility add "high"
        temperatureMaxTextView.setContentDescription("high " + temperatureMax);

        String temperatureMin = Utility.formatTemperature(getActivity(),
                weatherTemperatureMin,
                Utility.isMetric(getActivity()));
        temperatureMinTextView.setText(temperatureMin);
        // accessibility add "low"
        temperatureMinTextView.setContentDescription("low " + temperatureMin);

        String humidityString = "Humidity: "
                + String.valueOf((int)weatherHumidity)
                + " %";
        humidityTextView.setText(humidityString);

        String pressureString = "Pressure: "
                + String.valueOf((int)weatherPressure)
                + " hPa";
        pressureTextView.setText(pressureString);

        String windString = "Wind: "
                + String.valueOf((int)weatherWindSpeed)
                + " km/hr "
                + WeatherHelper.windDirectionCompassPointForWindDegrees(weatherWindDegrees);
        windTextView.setText(windString);
        windTextView.setContentDescription(WeatherHelper.windContentDescriptionForSpeedAndDegrees(weatherWindSpeed,
                weatherWindDegrees));

        windCompassView.mWindDegrees = weatherWindDegrees;
        // invalidate to redraw view in 2 pane mode
        // not needed in single pane mode
        windCompassView.invalidate();

        // setContentDescription null to avoid redundant accessibility TalkBalk audio to windTextView.
        windCompassView.setContentDescription(null);
    }

    void onLocationChanged(String newLocation) {
        // replace the mUri, since the location has changed
        if (mUri != null) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(mUri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }
}