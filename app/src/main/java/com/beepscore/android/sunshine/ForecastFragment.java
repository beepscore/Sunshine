package com.beepscore.android.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    List<String> weekForecast = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentMainView = inflater.inflate(R.layout.fragment_forecast, container, false);
        configureList();

        // adapter creates views for each list item
        ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);

        ListView listView = (ListView)fragmentMainView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);

        return fragmentMainView;
    }

    private void configureList() {
        weekForecast.add("Today - Sunny - 88/63");
        weekForecast.add("Tomorrow - Foggy - 70/46");
        weekForecast.add("Weds - Cloudy - 72/63");
        weekForecast.add("Thurs - Rainy - 64/51");
        weekForecast.add("Fri - Foggy - 70/46");
        weekForecast.add("Sat - Sunny - 76/68");
    }

}
