package com.beepscore.android.sunshine;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
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
