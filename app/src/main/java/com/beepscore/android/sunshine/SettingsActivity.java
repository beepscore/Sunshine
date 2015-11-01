package com.beepscore.android.sunshine;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.beepscore.android.sunshine.sync.SunshineSyncAdapter;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        // To target SDK 10 Gingerbread, must use deprecated addPreferencesFromResource
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary
     * (so it shows up before the value is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
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
        } else {
            // For non list preferences, set the summary to the value's simple string representation.
            // e.g. EditText preference such as location will use this
            preference.setSummary(stringValue);

            if (preference.getTitle().toString().equals("Location")) {
                Log.e(LOG_TAG, "preference "
                        + preference.getTitle().toString()
                        + " "
                        + stringValue);
                LocationStatusUtils.setLocationStatusUnknown(this);
            }
        }

        SunshineSyncAdapter.syncImmediately(getApplicationContext());
        return true;
    }

    // TargetApi annotation only adds method if api is Jelly Bean or later
    // avoids error from undefined method
    // getParentActivityIntent added in API level 16, didn't exist before Jelly Bean
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    /**
     * fix bug per lesson video
     * check deployment target, may not need this code
     */
    public Intent getParentActivityIntent() {
        // FLAG_ACTIVITY_CLEAR_TOP indicates if MainActivity is already running
        // use it instead of instantiating a new MainActivity
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

}
