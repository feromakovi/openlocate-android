/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.openlocate.android.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteFullException;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.location.LocationResult;

import java.lang.ref.WeakReference;
import java.util.List;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {

    private final static String TAG = LocationUpdatesBroadcastReceiver.class.getSimpleName();

    static final String ACTION_PROCESS_UPDATES = ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.contains(ACTION_PROCESS_UPDATES)) {
                LocationResult locationResult = LocationResult.extractResult(intent);
                if (locationResult != null) {
                    new PersistLocationUpdatesTask(goAsync(), context, locationResult).execute();
                }
            }
        }
    }

    private static class PersistLocationUpdatesTask extends AsyncTask<Void, Void, Boolean> {

        private PendingResult pendingResult;
        private WeakReference<Context> context;
        private LocationResult locationResult;

        PersistLocationUpdatesTask(PendingResult pendingResult, Context context, LocationResult locationResult) {
            this.pendingResult = pendingResult;
            this.context = new WeakReference<>(context);
            this.locationResult = locationResult;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Context context = this.context.get();
            List<Location> locations = locationResult.getLocations();
            if (context == null || locations == null || locations.isEmpty() == true) {
                return true;
            }

            try {
                OpenLocate.Configuration configuration = OpenLocate.getInstance().getConfiguration();
                AdvertisingIdClient.Info advertisingIdInfo = OpenLocate.getInstance().getAdvertisingIdInfo();

                if (configuration != null && advertisingIdInfo != null) {
                    processLocations(locations, context, configuration, advertisingIdInfo);
                }
            } catch (IllegalStateException e) {
                Log.w(TAG, "Could not getInstance() of OL.");
            } catch (RuntimeException e) {
                Log.e(TAG, "Could not persist ol updates.");
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pendingResult != null) {
                pendingResult.finish();
            }
        }

        private void processLocations(List<Location> locations, Context context,
                                      OpenLocate.Configuration configuration,
                                      AdvertisingIdClient.Info advertisingIdInfo) {

            LocationDatabase locationsDatabase = new LocationDatabase(DatabaseHelper.getInstance(context));
            try {
                for (Location location : locations) {

                    Log.v(TAG, location.toString());

                    OpenLocateLocation olLocation = OpenLocateLocation.from(
                            location,
                            advertisingIdInfo,
                            InformationFieldsFactory.collectInformationFields(context, configuration)
                    );

                    locationsDatabase.add(olLocation);
                }
            } catch (SQLiteFullException exception) {
                Log.w(TAG, "Database is full. Cannot add data.");
            } finally {
                locationsDatabase.close();
            }
        }
    }
}
