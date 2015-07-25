package com.beepscore.android.sunshine;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    // public empty constructor
    public ForecastFragment() {
    }

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

        ListView listView = (ListView)fragmentForecastView.findViewById(R.id.listview_forecast);
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

    /*
    private void updateAdapter(ArrayAdapter adapter, ArrayList<String> forecastStrings) {
        adapter.clear();
        adapter.addAll(forecastStrings);
    }
    */

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

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchWeatherForLocationPreference() {
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity(), adapter);
        String locationPreferenceString = PreferenceHelper.getLocationPreferenceString(getActivity());
        fetchWeatherTask.execute(locationPreferenceString);
    }

}
