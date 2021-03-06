package com.beepscore.android.sunshine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;

import com.beepscore.android.sunshine.LocationStatusUtils;
import com.beepscore.android.sunshine.MainActivity;
import com.beepscore.android.sunshine.PreferenceHelper;
import com.beepscore.android.sunshine.R;
import com.beepscore.android.sunshine.Utility;
import com.beepscore.android.sunshine.WeatherHelper;
import com.beepscore.android.sunshine.data.WeatherContract;
import com.beepscore.android.sunshine.data.WeatherProvider;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the weather, in seconds.
    // seconds = hours * 60 minutes/hour * 60 seconds/minute
    // use a short interval for manual testing.
    // SYNC_INTERVAL = 30 resulted in about 1 call to onPerformSync every ~ 60 seconds.
    // Maybe inexact timers are very lenient??
    // Maybe downloading and parsing caused it to not be ready after 30 seconds?? Sounds wrong.
    // public static final int SYNC_INTERVAL = 30;
    public static final int SYNC_INTERVAL = 3 * 60 * 60;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;
    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Log.d("SunshineSyncAdapter", "configurePeriodicSync");

        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle extras,
                              String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {

        Log.d(LOG_TAG, "onPerformSync");

        String locationQuery = Utility.getPreferredLocation(getContext());

        if (locationQuery == null) {
            return;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        int numDays = 14;

        // set unknown until we get a response with more info
        LocationStatusUtils.setLocationStatus(getContext(), LocationStatusUtils.LOCATION_STATUS_UNKNOWN);

        try {
            Uri builtUri = WeatherHelper.weatherUri(locationQuery, format, units, numDays);
            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                LocationStatusUtils.setLocationStatus(getContext(), LocationStatusUtils.LOCATION_STATUS_UNKNOWN);
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                LocationStatusUtils.setLocationStatus(getContext(), LocationStatusUtils.LOCATION_STATUS_SERVER_DOWN);
                return;
            }
            forecastJsonStr = buffer.toString();
            if (forecastJsonStr.contains("Error: Not found city")) {
                Log.e(LOG_TAG, "Error: Not found city");
                LocationStatusUtils.setLocationStatus(getContext(), LocationStatusUtils.LOCATION_STATUS_UNKNOWN);
                return;
            }

            getWeatherDataFromJson(forecastJsonStr, locationQuery);

        } catch (UnknownHostException e) {
            // UnknownHostException extends IOException
            Log.e(LOG_TAG, "UnknownHostException ", e);
            LocationStatusUtils.setLocationStatus(getContext(), LocationStatusUtils.LOCATION_STATUS_SERVER_DOWN);
        } catch (FileNotFoundException e) {
            // FileNotFoundException extends IOException
            Log.e(LOG_TAG, "FileNotFoundException ", e);
            LocationStatusUtils.setLocationStatus(getContext(), LocationStatusUtils.LOCATION_STATUS_SERVER_DOWN);
        } catch (SocketException e) {
            // SocketException extends IOException
            Log.e(LOG_TAG, "SocketException ", e);
            LocationStatusUtils.setLocationStatus(getContext(), LocationStatusUtils.LOCATION_STATUS_SERVER_DOWN);
        } catch (IOException e) {
            // catch any remaining IOExceptions not already caught above
            // for example if forecastJsonStr == null
            Log.e(LOG_TAG, "IOException ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            LocationStatusUtils.setLocationStatus(getContext(), LocationStatusUtils.LOCATION_STATUS_SERVER_DOWN);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            LocationStatusUtils.setLocationStatus(getContext(), LocationStatusUtils.LOCATION_STATUS_SERVER_INVALID);
        } finally {
            Log.v(LOG_TAG, "location status " + LocationStatusUtils.getLocationStatus(getContext()));
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    /**
     * Parses forecastJsonStr
     * This method has side effects
     * inserts values in content provider and calls notifyWeather
     * @param forecastJsonStr the complete forecast in JSON format
     */
    public void getWeatherDataFromJson(String forecastJsonStr,
                                       String locationSetting)
            throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        // Location information
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        // Location coordinate
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OWM_LIST = "list";

        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        // All temperatures are children of the "temp" object.
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_MESSAGE_CODE = "cod";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            if (forecastJson.has(OWM_MESSAGE_CODE)) {
                int owmStatusCode = forecastJson.getInt(OWM_MESSAGE_CODE);

                switch (owmStatusCode) {
                    case HttpURLConnection.HTTP_OK: {
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        // HTTP_NOT_FOUND == status code 404
                        LocationStatusUtils.setLocationStatus(getContext(),
                                LocationStatusUtils.LOCATION_STATUS_INVALID);
                        return;
                    }
                    default: {
                        LocationStatusUtils.setLocationStatus(getContext(),
                                LocationStatusUtils.LOCATION_STATUS_SERVER_DOWN);
                        return;
                    }
                }
            }

            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

            // OpenWeatherMap OWM returns daily forecasts based upon the local time
            // of the city that is being asked for,
            // which means that we need to know the GMT offset to translate this data properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            for(int i = 0; i < weatherArray.length(); i++) {
                // These are the values that will be collected.
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                // Description is in a child array called "weather", which is 1 element long.
                // That element also contains a weather code.
                JSONObject weatherObject =
                        dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                cVVector.add(weatherValues);
            }

            int inserted = 0;
            if ( cVVector.size() > 0 ) {

                // add to database
                // evaluates to content://com.beepscore.android.sunshine/weather
                Uri weatherUri = WeatherContract.WeatherEntry.CONTENT_URI;

                ContentValues[] weatherContentValues = cVVector.toArray(new ContentValues[0]);
                // call bulkInsert to add the weatherEntries to the database
                inserted = getContext().getContentResolver().bulkInsert(weatherUri, weatherContentValues);

                // delete old data
                String[] selectionArgs = new String[]{Long.toString(dayTime.setJulianDay(julianStartDay))};

                int numberOfRowsDeleted = getContext().getContentResolver().delete(weatherUri,
                        WeatherProvider.sBeforeDateSelection,
                        selectionArgs);
                Log.d(LOG_TAG, "numberOfRowsDeleted " + String.valueOf(numberOfRowsDeleted));

                notifyWeather();
            }

            Log.d(LOG_TAG, "getWeatherDataFromJson Complete. " + inserted + " Inserted");
            LocationStatusUtils.setLocationStatus(getContext(), LocationStatusUtils.LOCATION_STATUS_OK);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            LocationStatusUtils.setLocationStatus(getContext(), LocationStatusUtils.LOCATION_STATUS_SERVER_INVALID);
        }
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     *                        e.g. "Sunnydale, CA"
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    public long addLocation(String locationSetting, String cityName, double lat, double lon) {

        Cursor locationCursor = getLocationCursor(locationSetting);
        long locationRowId = -1;

        if (locationCursor != null
                && locationCursor.moveToFirst()) {
            // location with this locationSetting already exists in content provider
            // http://stackoverflow.com/questions/2848056/how-to-get-a-row-id-from-a-cursor
            locationRowId = locationCursor.getLong(locationCursor.getColumnIndex("_id"));

        } else if (locationSetting != null && cityName != null) {
            locationRowId = addNewLocation(locationSetting, cityName, lat, lon);
        }

        if (locationCursor != null) {
            locationCursor.close();
        }

        return locationRowId;
    }

    /**
     * queries content provider
     * @param locationSetting The location string used to request updates from the server.
     * Note multiple locationSettings could have the same cityName.
     * @return a Cursor with location _IDs that match locationSetting
     */
    private Cursor getLocationCursor(String locationSetting) {
        // Query content provider, not database
        // More flexible design, easier to change content provider to use a different underlying source

        // e.g. "content://com.beepscore.android.sunshine/location"
        Uri locationUri = WeatherContract.LocationEntry.CONTENT_URI;
        // we need only column _ID
        String[] projection = {WeatherContract.LocationEntry._ID};
        String selection = WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING  + " = ? ";
        String[] selectionArgs = {locationSetting};

        return getContext().getContentResolver().query(
                locationUri,
                projection,
                selection,
                selectionArgs,
                null  // sort order
        );
    }

    private long addNewLocation(String locationSetting, String cityName, double lat, double lon) {

        Uri locationUri = WeatherContract.LocationEntry.CONTENT_URI;

        ContentValues contentValues = new ContentValues();
        contentValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
        contentValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
        contentValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
        contentValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

        // e.g. content://com.beepscore.android.sunshine/location/2
        Uri locationRowUri = getContext().getContentResolver().insert(locationUri, contentValues);
        long locationRowId = Long.valueOf(locationRowUri.getLastPathSegment());
        return locationRowId;
    }

    private void notifyWeather() {
        Context context = getContext();

        // In Sunshine, can tap Settings / Refresh to generate a notification

        //check the last update and notify if it's the first of the day
        long lastSync = PreferenceHelper.getLastSyncPreference(context);

        //int TIME_BETWEEN_NOTIFICATIONS_MIN_MSEC = DAY_IN_MILLIS;
        // for testing use a short time
        int TIME_BETWEEN_NOTIFICATIONS_MIN_MSEC = 10000;

        if (PreferenceHelper.getEnableNotificationPreference(context)
                && (System.currentTimeMillis() - lastSync >= TIME_BETWEEN_NOTIFICATIONS_MIN_MSEC)) {

            // Last sync was more than 1 day ago, let's send a notification with the weather.
            String locationQuery = Utility.getPreferredLocation(context);

            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

            // we'll query our contentProvider, as always
            Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

            if (cursor.moveToFirst()) {
                int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                double high = cursor.getDouble(INDEX_MAX_TEMP);
                double low = cursor.getDouble(INDEX_MIN_TEMP);
                String desc = cursor.getString(INDEX_SHORT_DESC);

                int iconId = WeatherHelper.getIconResourceForWeatherCondition(weatherId);
                Resources resources = context.getResources();
                int artResourceId = WeatherHelper.getArtResourceForWeatherCondition(weatherId);
                String artUrl = WeatherHelper.getArtUrlForWeatherCondition(context, weatherId);

                // On Honeycomb and higher devices, we can retrieve the size of the large icon
                // Prior to that, we use a fixed size
                @SuppressLint("InlinedApi")
                int largeIconWidth = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
                        : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);
                @SuppressLint("InlinedApi")
                int largeIconHeight = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
                        : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);

                // Retrieve the large icon
                Bitmap largeIcon;
                try {
                    // get() is a blocking call, but it's ok to block this background thread
                    largeIcon = Glide.with(context)
                            .load(artUrl)
                            .asBitmap()
                            .error(artResourceId)
                            .fitCenter()
                            .into(largeIconWidth, largeIconHeight)
                            .get();
                } catch (InterruptedException | ExecutionException e) {
                    Log.e(LOG_TAG, "Error retrieving large icon from " + artUrl, e);
                    largeIcon = BitmapFactory.decodeResource(resources, artResourceId);
                }
                String title = context.getString(R.string.app_name);

                // Define the text of the forecast.

                // String contentText = String.format(context.getString(R.string.format_notification),
                //         desc,
                //         Utility.formatTemperature(context, high),
                //         Utility.formatTemperature(context, low));
                // see gist comments
                // https://gist.github.com/udacityandroid/e5eb3afa254ca750e083
                boolean isMetric = Utility.isMetric(context);
                String contentText = String.format(context.getString(R.string.format_notification),
                        desc,
                        Utility.formatTemperature(context, high, isMetric),
                        Utility.formatTemperature(context, low, isMetric));

                ///////////////////////////////////////////////////////////////
                // Build notification
                // http://www.vogella.com/tutorials/AndroidNotifications/article.html
                // http://developer.android.com/guide/topics/ui/notifiers/notifications.html

                // explicit intent for what the notification should open
                Intent intent = new Intent(context, MainActivity.class);

                // Create an artificial "backstack" for user to tap Back
                TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
                taskStackBuilder.addNextIntent(intent);
                // pIntent is triggered if the notification is selected
                // use System.currentTimeMillis() to have a unique ID for the pending intent
                PendingIntent pIntent = taskStackBuilder.getPendingIntent((int) System.currentTimeMillis(),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                // http://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html
                Notification notification = new NotificationCompat.Builder(context)
                        .setContentTitle(title)
                        .setContentText(contentText)
                        .setColor(resources.getColor(R.color.sunshine_light_blue))
                        .setSmallIcon(iconId)
                        .setLargeIcon(largeIcon)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .build();

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(WEATHER_NOTIFICATION_ID, notification);

                ///////////////////////////////////////////////////////////////

                PreferenceHelper.setLastSyncPreference(context, System.currentTimeMillis());
            }
        }

    }

}