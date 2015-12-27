
/**
 * Created by stevebaker on 12/26/15.
 */
/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
        package com.beepscore.android.sunshine.gcm;

        import android.app.IntentService;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.preference.PreferenceManager;
        import android.util.Log;
        import android.widget.Toast;

        import com.beepscore.android.sunshine.MainActivity;
        import com.beepscore.android.sunshine.R;
        import com.google.android.gms.gcm.GoogleCloudMessaging;
        import com.google.android.gms.iid.InstanceID;
        import com.google.android.gms.gcm.GoogleCloudMessaging;
        import com.google.android.gms.iid.InstanceID;


public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                InstanceID instanceID = InstanceID.getInstance(this);

                String senderId = getString(R.string.gcm_defaultSenderId);
                if ( senderId.length() != 0 ) {
                    String token = instanceID.getToken(senderId,
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    sendRegistrationToServer(token);
                }

                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.
                sharedPreferences.edit().putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, true).apply();
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);

            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    /**
     * When our third party server requests google cloud messaging to push a notification
     * it supplies a server API key and a list of registration_id tokens.
     *
     * Server api key
     * Approximately 39 characters long including 2 dashes.
     * Keep this a secret, store it securely on server.
     *
     * Registration_id tokens
     * The list contains one token for every device app instance we are pushing to.
     * Normally, we would persist each token on the third-party server.
     *
     * For this sample app we don't have a server, and are using a website.
     * http://udacity.github.io/Advanced_Android_Development/
     *
     * Alternatively can use POSTMAN to manually send a push notification request.
     * POST https://android.googleapis.com/gcm/send
     * header
     * Authorization: "key=<server api key>"
     * Content-type: application/json
     * Body
     * {"data": {"weather": "Avalanche", "location": "Florida"},
     *  "registration_ids": ["<registration_id0>", "<registration_id1>"] }
     *
     * @param token The new token, also known as registration_id.
     *              Uniquely identifies an app instance installed on a single device.
     *              length ~ 152 characters
     */
    private void sendRegistrationToServer(String token) {
        // Log the token to enable manually recording it and
        // manually sending push notification request.
        Log.i(TAG, "GCM Registration ID Token: " + token);
    }
}
