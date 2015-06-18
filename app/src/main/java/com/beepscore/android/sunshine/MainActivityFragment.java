package com.beepscore.android.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

//import java.text.DateFormat;
import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentMainView = inflater.inflate(R.layout.fragment_main, container, false);
        configureList();
        return fragmentMainView;
    }

    private void configureList() {
        List<String> list = new ArrayList<String>;

        list.add("Today - Sunny - 88/63");
        list.add("Tomorrow - Foggy - 70/46");
        list.add("Weds - Cloudy - 72/63");
        list.add("Thurs - Rainy - 64/51");
        list.add("Fri - Foggy - 70/46");
        list.add("Sat - Sunny - 76/68");
    }

}
