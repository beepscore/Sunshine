package com.beepscore.android.sunshine;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.beepscore.android.sunshine.data.WeatherContract;
import com.beepscore.android.sunshine.sync.SunshineSyncAdapter;

/**
 * Created by stevebaker on 11/1/15.
 */
public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
    }

    @Override
    public void onPause() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pref.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pref.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary
     * (so it shows up before the value is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's current value.
        // TODO: This may be calling onPreferenceChange more frequently than desired
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference.getKey().equals(getString(R.string.pref_location_key))) {
            // In view row top line is preference title e.g. "Location"
            // In view row bottom line is preference summary.
            String locationSummary;
            int locationStatus = LocationStatusUtils.getLocationStatus(getActivity());
            switch (locationStatus) {
                case LocationStatusUtils.LOCATION_STATUS_INVALID: {
                    // e.g. "Invalid Location (Londan)"
                    locationSummary = getString(R.string.pref_location_error_description, value);
                    break;
                }
                case LocationStatusUtils.LOCATION_STATUS_UNKNOWN: {
                    locationSummary = getString(R.string.pref_location_unknown_description, value);
                    break;
                }
                default: {
                    locationSummary = value.toString();
                }
            }
            preference.setSummary(locationSummary);
        } else {
            // For non list preferences, set the summary to the value's simple string representation.
            // e.g. EditText preference such as location will use this
            preference.setSummary(stringValue);
        }
        //SunshineSyncAdapter.syncImmediately(getActivity());
        return true;
    }

    // This gets called after the preference is changed, which is important because we
    // start our synchronization here
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        if (key.equals(getString(R.string.pref_location_key))) {
            LocationStatusUtils.setLocationStatusUnknown(getActivity());
            SunshineSyncAdapter.syncImmediately(getActivity());
        }

        if (key.equals(getString(R.string.pref_units_key))) {
            // units have changed. update lists of weather entries accordingly
            getActivity().getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI,
                    null);
        }
    }

}
