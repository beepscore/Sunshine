package com.beepscore.android.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    // public empty constructor
    public ForecastFragment() {
    }

    ListView listView = null;
    ArrayAdapter adapter = null;

    @Override
    // onCreate is called before onCreateView
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentForecastView = inflater.inflate(R.layout.fragment_forecast, container, false);

        List<String> weekForecast = new ArrayList<String>();

        // adapter creates views for each list item
        adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);

        listView = (ListView)fragmentForecastView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);

        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        fetchWeatherTask.execute("98053");

        return fragmentForecastView;
    }

    private void updateAdapter(ArrayAdapter adapter, String[] forecastStrings) {
        adapter.clear();
        adapter.addAll(forecastStrings);
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
            FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
            fetchWeatherTask.execute("98053");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////////////////

    // https://developer.android.com/guide/components/processes-and-threads.html#Threads
    // http://stackoverflow.com/questions/9671546/asynctask-android-example?rq=1
    // Nested classes
    // http://docs.oracle.com/javase/tutorial/java/javaOO/nested.html
    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        /** The system calls this on a worker (background) thread
         *  @param params first and only element is postcode string to make request to web service
         *  @return json response from web service. return null if no input.
         */
        @Override
        protected String[] doInBackground(String... params) {

            String postcode = params[0];
            final Integer NUM_DAYS = 7;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String[] forecastStrings = null;

            try {

                Uri weatherUri = WeatherHelper.weatherUri(postcode, "json", "metric", NUM_DAYS);
                URL weatherUrl = new URL(weatherUri.toString());

                urlConnection = (HttpURLConnection) weatherUrl.openConnection();
                urlConnection.setRequestMethod("GET");

                // If call connect on main thread app will crash
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
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
                    return null;
                }
                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;

            } finally {
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

            try {
                WeatherDataParser weatherDataParser = new WeatherDataParser();
                forecastStrings = weatherDataParser.getWeatherDataFromJson(forecastJsonStr, NUM_DAYS);
                return forecastStrings;
            } catch (JSONException e) {
                // Couldn't parse json
                Log.e(LOG_TAG, "Error ", e);
                e.printStackTrace();
                return null;
            }
        }

        /** Use to update UI. The system calls this on the UI thread.
         *  @param forecastStrings is the result returned from doInBackground()
         */
        @Override
        protected void onPostExecute(String[] forecastStrings) {
            super.onPostExecute(forecastStrings);
            updateAdapter(adapter, forecastStrings);
        }

    }

}
