package com.beepscore.android.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


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
        //configureDateViewInView(fragmentMainView);
        return fragmentMainView;
    }

    /*
    private void configureDateViewInView(View fragmentMainView) {
        // Android TextClock would be nice, but requires SDK >= 17. So don't use it.
        // https://developer.android.com/reference/android/widget/TextClock.html

        TextView dateView = (TextView) fragmentMainView.findViewById(R.id.dateView);

        // use user's preselected settings
        DateFormat dateFormat = DateFormat.getDateInstance();

        // Get calendar with default locale and time zone. Don't assume Gregorian
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        String formattedDate = dateFormat.format(now);
        dateView.setText(formattedDate);
    }
    */
}
