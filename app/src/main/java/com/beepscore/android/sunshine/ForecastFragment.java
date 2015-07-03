package com.beepscore.android.sunshine;

import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    // public empty constructor
    public ForecastFragment() {
    }

    ListView listView = null;
    ArrayAdapter<String> adapter = null;

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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String dayForecast = adapter.getItem(i);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, dayForecast);
                startActivity(intent);
            }
        });

        return fragmentForecastView;
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchWeatherForLocationPreference();
    }

    private void updateAdapter(ArrayAdapter adapter, ArrayList<String> forecastStrings) {
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
            fetchWeatherForLocationPreference();
            return true;
        }

        if (id == R.id.intent_map) {
            String locatonPreference = PreferenceHelper.getLocationPreferenceString(getActivity());
            showMapForLocationPreference(locatonPreference);
            return true;
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchWeatherForLocationPreference() {
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        String locationPreferenceString = PreferenceHelper.getLocationPreferenceString(getActivity());
        fetchWeatherTask.execute(locationPreferenceString);
    }

    ////////////////////////////////////////////////////////////////////////////

    // https://developer.android.com/guide/components/processes-and-threads.html#Threads
    // http://stackoverflow.com/questions/9671546/asynctask-android-example?rq=1
    // Nested classes
    // http://docs.oracle.com/javase/tutorial/java/javaOO/nested.html
    // first parameter is input, second is integer for onProgressUpdate, third is return for onPostExecute
    private class FetchWeatherTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        /** The system calls this on a worker (background) thread
         *  @param params first and only element is postcode string to make request to web service
         *  @return json response from web service. return null if no input.
         */
        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(String... params) {

            String postcode = params[0];
            final Integer NUM_DAYS = 7;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {

                Uri weatherUri = WeatherHelper.weatherJsonMetricUri(postcode, NUM_DAYS);
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
                ArrayList<HashMap<String, String>> forecastResults = weatherDataParser.getWeatherDataFromJson(forecastJsonStr, NUM_DAYS);
                return forecastResults;
            } catch (JSONException e) {
                // Couldn't parse json
                Log.e(LOG_TAG, "Error ", e);
                e.printStackTrace();
                return null;
            }
        }

        /** Use to update UI. The system calls this on the UI thread.
         *  @param forecastResults is the result returned from doInBackground()
         */
        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> forecastResults) {
            super.onPostExecute(forecastResults);
            if (forecastResults != null) {

                ArrayList<String> forecastStrings = new ArrayList<String>();

                for (int index = 0; index < forecastResults.size(); ++index) {
                    HashMap<String, String> dayMap = forecastResults.get(index);

                    String forecastString = "";
                    if (dayMap != null) {

                        if (dayMap.containsKey(WeatherDataParser.DAY)
                                && (dayMap.get(WeatherDataParser.DAY) != null)) {
                            String descriptionString = dayMap.get(WeatherDataParser.DAY);
                            forecastString = forecastString + descriptionString;
                        }

                        forecastString = forecastString + " ";

                        if (dayMap.containsKey(WeatherDataParser.DESCRIPTION)
                                && (dayMap.get(WeatherDataParser.DESCRIPTION) != null)) {
                            String descriptionString = dayMap.get(WeatherDataParser.DESCRIPTION);
                            forecastString = forecastString + descriptionString;
                        }

                        forecastString = forecastString + " ";

                        if (dayMap.containsKey(WeatherDataParser.OWM_MAX)
                                && (dayMap.get(WeatherDataParser.OWM_MAX) != null)) {
                            String maxString = dayMap.get(WeatherDataParser.OWM_MAX);
                            String temperatureInPreferredUnits = temperatureInPreferredUnits(maxString);
                            forecastString = forecastString + temperatureInPreferredUnits;
                        }

                        forecastString = forecastString + " / ";

                        if (dayMap.containsKey(WeatherDataParser.OWM_MIN)
                                && (dayMap.get(WeatherDataParser.OWM_MIN) != null)) {
                            String minString = dayMap.get(WeatherDataParser.OWM_MIN);
                            String temperatureInPreferredUnits = temperatureInPreferredUnits(minString);
                            forecastString = forecastString + temperatureInPreferredUnits;
                        }

                        forecastString = forecastString + " humidity: ";

                        if (dayMap.containsKey(WeatherDataParser.OWM_HUMIDITY)
                                && (dayMap.get(WeatherDataParser.OWM_HUMIDITY) != null)) {
                            String humidityString = dayMap.get(WeatherDataParser.OWM_HUMIDITY);
                            forecastString = forecastString + humidityString;
                        }

                    }
                    forecastStrings.add(forecastString);
                }
                updateAdapter(adapter, forecastStrings);
            }
        }

    }

    private String temperatureInPreferredUnits(String temperatureDegreesCString) {
        double temperatureDegreesC = Double.parseDouble(temperatureDegreesCString);
        // getIntegerInstance rounds, doesn't truncate.
        NumberFormat formatter = NumberFormat.getIntegerInstance();

        if (PreferenceHelper.getUnitsPreferenceString(getActivity()) == getString(R.string.pref_units_imperial)) {
            return formatter.format(WeatherHelper.degreesCToDegreesF(temperatureDegreesC))
                    + getString(R.string.degreesF);
        } else {
            return formatter.format(temperatureDegreesC) + getString(R.string.degreesC);
        }
    }

    public void showMapForLocationPreference(String locationPreference) {
        Uri geoLocation = getGeoLocation(locationPreference);
        showMapForUri(geoLocation);
    }

    public Uri getGeoLocation(String locationPreference) {
        String escapedLocationPreference = locationPreference.replaceAll(" ", "+");
        Uri uri = Uri.parse("geo:0,0?q=" + escapedLocationPreference);
        return uri;
    }

    // https://developer.android.com/guide/components/intents-common.html
    public void showMapForUri(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}
