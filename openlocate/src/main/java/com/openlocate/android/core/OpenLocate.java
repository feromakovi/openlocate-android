/*
 * Copyright (c) 2017 OpenLocate
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.openlocate.android.core;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.openlocate.android.exceptions.InvalidConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class OpenLocate {

    private static final String TAG = OpenLocate.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static OpenLocate sharedInstance = null;

    private Context context;
    private OpenLocateHelper openLocateHelper;

    private Configuration configuration;
    private AdvertisingIdClient.Info advertisingIdInfo;

    private OpenLocate(Configuration configuration) {
        this.context = configuration.context;
        this.openLocateHelper = new OpenLocateHelper(context, configuration);
        this.configuration = configuration;
    }

    public static OpenLocate initialize(Configuration configuration) {

        saveConfiguration(configuration);

        if (sharedInstance == null) {
            sharedInstance = new OpenLocate(configuration);
        }

        boolean trackingEnabled = SharedPreferenceUtils.getInstance(configuration.context).getBoolanValue(Constants.TRACKING_STATUS, false);

        if (trackingEnabled && hasLocationPermission(configuration.context) &&
                sharedInstance.isGooglePlayServicesAvailable() == ConnectionResult.SUCCESS) {
            sharedInstance.onPermissionsGranted();
        }

        return sharedInstance;
    }

    public static OpenLocate getInstance() throws IllegalStateException {
        if (sharedInstance == null) {
            throw new IllegalStateException("OpenLate SDK must be initialized using initialize method first");
        }
        return sharedInstance;
    }

    public void startTracking(Activity activity) {

        if (configuration == null) {
            return;
        }

        int resultCode = isGooglePlayServicesAvailable();
        if (resultCode != ConnectionResult.SUCCESS) {
            if (activity != null) {
                GoogleApiAvailability.getInstance().getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return;
        }

        SharedPreferenceUtils.getInstance(context).setValue(Constants.TRACKING_STATUS, true);

        if (hasLocationPermission(context)) {
            onPermissionsGranted();
        } else if (activity != null) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            startCheckingPermissionTask();
        } else {
            Log.w(TAG, "Location Permission has not been accepted or prompted.");
        }
    }

    public void stopTracking() {
        SharedPreferenceUtils.getInstance(context).setValue(Constants.TRACKING_STATUS, false);
        openLocateHelper.stopTracking();
    }

    public void updateConfiguration(OpenLocate.Configuration configuration) {
        saveConfiguration(configuration);
        this.configuration = configuration;
        this.openLocateHelper.updateConfiguration(configuration);
    }

    public boolean isTracking() {
        return SharedPreferenceUtils.getInstance(context).getBoolanValue(Constants.TRACKING_STATUS, false);
    }

    protected OpenLocate.Configuration getConfiguration() {
        return configuration;
    }

    protected AdvertisingIdClient.Info getAdvertisingIdInfo() {
        return advertisingIdInfo;
    }

    private int isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        return apiAvailability.isGooglePlayServicesAvailable(context);
    }

    private void startCheckingPermissionTask() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (hasLocationPermission(context)) {
                    onPermissionsGranted();
                    this.cancel();
                }

            }
        }, 5 * 1000, 5 * 1000);
    }

    private void onPermissionsGranted() {
        FetchAdvertisingInfoTask task = new FetchAdvertisingInfoTask(context, new FetchAdvertisingInfoTaskCallback() {
            @Override
            public void onAdvertisingInfoTaskExecute(AdvertisingIdClient.Info info) {
                advertisingIdInfo = info;
                SharedPreferenceUtils.getInstance(context).setValue(Constants.ADVERTISING_ID, info.getId());
                SharedPreferenceUtils.getInstance(context).setValue(Constants.ADVERTISING_ID_TRACKING, info.isLimitAdTrackingEnabled());
                openLocateHelper.startTracking();
            }
        });
        task.execute();
    }

    private static void saveConfiguration(Configuration configuration) throws InvalidConfigurationException {
        if (configuration.endpoints.isEmpty()) {
            String message = "Invalid configuration. Please configure a valid urls";

            Log.e(TAG, message);

            throw new InvalidConfigurationException(
                    message
            );
        }

        try {
            String endpoins = Endpoint.toJson(configuration.endpoints);
            SharedPreferenceUtils.getInstance(configuration.context).setValue(Constants.ENDPOINTS_KEY, endpoins);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static boolean isLocationEnabled(Context context) throws IllegalStateException {
        try {
            int locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } catch (Settings.SettingNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static boolean hasLocationPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    public static final class Configuration implements Parcelable {

        Context context = null;
        private List<Endpoint> endpoints;

        private String serverUrl;
        private HashMap<String, String> headers;


        private boolean isWifiCollectionDisabled;
        private boolean isDeviceModelCollectionDisabled;
        private boolean isDeviceManufacturerCollectionDisabled;
        private boolean isOperatingSystemCollectionDisbaled;
        private boolean isChargingInfoCollectionDisabled;
        private boolean isCarrierNameCollectionDisabled;
        private boolean isConnectionTypeCollectionDisabled;
        private boolean isLocationMethodCollectionDisabled;
        private boolean isLocationContextCollectionDisabled;

        public static final class Builder {
            private Context context;

            private List<Endpoint> endpoints;
            private String serverUrl;
            private HashMap<String, String> headers;

            private long transmissionInterval = Constants.DEFAULT_TRANSMISSION_INTERVAL_SEC;
            private long locationUpdateInterval = Constants.DEFAULT_LOCATION_INTERVAL_SEC;
            private LocationAccuracy locationAccuracy = Constants.DEFAULT_LOCATION_ACCURACY;

            private boolean isWifiCollectionDisabled;
            private boolean isDeviceModelCollectionDisabled;
            private boolean isDeviceManufacturerCollectionDisabled;
            private boolean isOperatingSystemCollectionDisbaled;
            private boolean isChargingInfoCollectionDisabled;
            private boolean isCarrierNameCollectionDisabled;
            private boolean isConnectionTypeCollectionDisabled;
            private boolean isLocationMethodCollectionDisabled;
            private boolean isLocationContextCollectionDisabled;

            public Builder(Context context, List<Endpoint> endpoints) {
                this.context = context.getApplicationContext();
                this.endpoints = endpoints;
            }

            public Builder(Context context, String serverUrl) {
                this.context = context.getApplicationContext();
                this.serverUrl = serverUrl;
            }

            public Builder(Context context, Endpoint endpoint) {
                this(context, Arrays.asList(endpoint));
            }

            public Builder setHeaders(HashMap<String, String> headers) {
                this.headers = headers;
                return this;
            }

            public Builder setTransmissionInterval(long seconds) {
                this.transmissionInterval = seconds;
                return this;
            }

            public Builder setLocationUpdateInterval(long seconds) {
                this.locationUpdateInterval = seconds;
                return this;
            }

            public Builder setLocationAccuracy(LocationAccuracy locationAccuracy) {
                this.locationAccuracy = locationAccuracy;
                return this;
            }

            public Builder withoutWifiInfo() {
                this.isWifiCollectionDisabled = true;
                return this;
            }

            public Builder withoutDeviceModel() {
                this.isDeviceModelCollectionDisabled = true;
                return this;
            }

            public Builder withoutDeviceManufacturer() {
                this.isDeviceManufacturerCollectionDisabled = true;
                return this;
            }

            public Builder withoutOperatingSystem() {
                this.isOperatingSystemCollectionDisbaled = true;
                return this;
            }

            public Builder withoutChargingInfo() {
                this.isChargingInfoCollectionDisabled = true;
                return this;
            }

            public Builder withoutCarrierName() {
                this.isCarrierNameCollectionDisabled = true;
                return this;
            }

            public Builder withoutConnectionType() {
                this.isConnectionTypeCollectionDisabled = true;
                return this;
            }

            public Builder withoutLocationMethod() {
                this.isLocationMethodCollectionDisabled = true;
                return this;
            }

            public Builder withoutLocationContext() {
                this.isLocationContextCollectionDisabled = true;
                return this;
            }

            public Configuration build() {
                if (serverUrl != null) {

                    Endpoint endpoint = new Endpoint(serverUrl, headers);

                    if (endpoints == null) {
                        endpoints = new ArrayList<>();
                    }

                    endpoints.add(endpoint);
                }
                return new Configuration(this);
            }
        }

        private Configuration(Builder builder) {
            this.context = builder.context;
            this.endpoints = builder.endpoints;
            this.isCarrierNameCollectionDisabled = builder.isCarrierNameCollectionDisabled;
            this.isChargingInfoCollectionDisabled = builder.isChargingInfoCollectionDisabled;
            this.isConnectionTypeCollectionDisabled = builder.isConnectionTypeCollectionDisabled;
            this.isDeviceManufacturerCollectionDisabled = builder.isDeviceManufacturerCollectionDisabled;
            this.isDeviceModelCollectionDisabled = builder.isDeviceModelCollectionDisabled;
            this.isLocationContextCollectionDisabled = builder.isLocationContextCollectionDisabled;
            this.isLocationMethodCollectionDisabled = builder.isLocationMethodCollectionDisabled;
            this.isOperatingSystemCollectionDisbaled = builder.isOperatingSystemCollectionDisbaled;
            this.isWifiCollectionDisabled = builder.isWifiCollectionDisabled;
        }

        public List<Endpoint> getEndpoints() {
            return endpoints;
        }

        public boolean isWifiCollectionDisabled() {
            return isWifiCollectionDisabled;
        }

        public boolean isDeviceModelCollectionDisabled() {
            return isDeviceModelCollectionDisabled;
        }

        public boolean isDeviceManufacturerCollectionDisabled() {
            return isDeviceManufacturerCollectionDisabled;
        }

        public boolean isOperaringSystemCollectionDisbaled() {
            return isOperatingSystemCollectionDisbaled;
        }

        public boolean isChargingInfoCollectionDisabled() {
            return isChargingInfoCollectionDisabled;
        }

        public boolean isCarrierNameCollectionDisabled() {
            return isCarrierNameCollectionDisabled;
        }

        public boolean isConnectionTypeCollectionDisabled() {
            return isConnectionTypeCollectionDisabled;
        }

        public boolean isLocationMethodCollectionDisabled() {
            return isLocationMethodCollectionDisabled;
        }

        public boolean isLocationContextCollectionDisabled() {
            return isLocationContextCollectionDisabled;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(this.endpoints);
            dest.writeString(this.serverUrl);
            dest.writeSerializable(this.headers);
            dest.writeByte(this.isWifiCollectionDisabled ? (byte) 1 : (byte) 0);
            dest.writeByte(this.isDeviceModelCollectionDisabled ? (byte) 1 : (byte) 0);
            dest.writeByte(this.isDeviceManufacturerCollectionDisabled ? (byte) 1 : (byte) 0);
            dest.writeByte(this.isOperatingSystemCollectionDisbaled ? (byte) 1 : (byte) 0);
            dest.writeByte(this.isChargingInfoCollectionDisabled ? (byte) 1 : (byte) 0);
            dest.writeByte(this.isCarrierNameCollectionDisabled ? (byte) 1 : (byte) 0);
            dest.writeByte(this.isConnectionTypeCollectionDisabled ? (byte) 1 : (byte) 0);
            dest.writeByte(this.isLocationMethodCollectionDisabled ? (byte) 1 : (byte) 0);
            dest.writeByte(this.isLocationContextCollectionDisabled ? (byte) 1 : (byte) 0);
        }

        protected Configuration(Parcel in) {
            this.endpoints = in.createTypedArrayList(Endpoint.CREATOR);
            this.serverUrl = in.readString();
            this.headers = (HashMap<String, String>) in.readSerializable();
            this.isWifiCollectionDisabled = in.readByte() != 0;
            this.isDeviceModelCollectionDisabled = in.readByte() != 0;
            this.isDeviceManufacturerCollectionDisabled = in.readByte() != 0;
            this.isOperatingSystemCollectionDisbaled = in.readByte() != 0;
            this.isChargingInfoCollectionDisabled = in.readByte() != 0;
            this.isCarrierNameCollectionDisabled = in.readByte() != 0;
            this.isConnectionTypeCollectionDisabled = in.readByte() != 0;
            this.isLocationMethodCollectionDisabled = in.readByte() != 0;
            this.isLocationContextCollectionDisabled = in.readByte() != 0;
        }

        public static final Creator<Configuration> CREATOR = new Creator<Configuration>() {
            @Override
            public Configuration createFromParcel(Parcel source) {
                return new Configuration(source);
            }

            @Override
            public Configuration[] newArray(int size) {
                return new Configuration[size];
            }
        };
    }

    public static class Endpoint implements Parcelable {

        public static final String URL = "url";
        public static final String HEADERS = "headers";
        public static final String HEADERS_KEY = "key";
        public static final String HEADERS_VALUE = "value";

        public static List<Endpoint> fromJson(String json) throws JSONException {

            JSONArray jsonArray = new JSONArray(json);
            List<Endpoint> result = new ArrayList<>(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonEndpoint = jsonArray.getJSONObject(i);
                Builder builder = Endpoint.builder(jsonEndpoint.getString(URL));
                JSONArray headers = jsonEndpoint.getJSONArray(HEADERS);
                for (int j = 0; j < headers.length(); j++) {
                    JSONObject header = headers.getJSONObject(j);
                    builder.withHeader(header.getString(HEADERS_KEY), header.getString(HEADERS_VALUE));
                }
                result.add(builder.build());
            }

            return result;
        }

        public static String toJson(List<Endpoint> endpoints) throws JSONException {
            JSONArray jsonArray = new JSONArray();

            for (Endpoint endpoint : endpoints) {

                JSONObject jsonEndpoint = new JSONObject();
                jsonEndpoint.put(URL, endpoint.url);


                JSONArray jsonHeaders = new JSONArray();

                for (Map.Entry<String, String> entry : endpoint.getHeaders().entrySet()) {
                    JSONObject header = new JSONObject();
                    header.put(HEADERS_KEY, entry.getKey());
                    header.put(HEADERS_VALUE, entry.getValue());
                    jsonHeaders.put(header);
                }

                jsonEndpoint.put(HEADERS, jsonHeaders);
                jsonArray.put(jsonEndpoint);
            }

            return jsonArray.toString();
        }

        private String url;

        private HashMap<String, String> headers;

        public Endpoint(String url, HashMap<String, String> headers) {
            this.url = url;

            if (headers == null) {
                this.headers = new HashMap<>();
            } else {
                this.headers = headers;
            }
        }

        private Endpoint(Builder builder) {
            this.url = builder.url;
            this.headers = builder.headers;
        }

        public String getUrl() {
            return url;
        }

        public HashMap<String, String> getHeaders() {
            return headers;
        }

        public static Builder builder(String url) {
            return new Builder(url);
        }

        public static class Builder {

            private String url;

            private HashMap<String, String> headers;

            public Builder(String url) {
                this.url = url;
            }

            public Builder withHeader(String key, String value) {

                if (headers == null) {
                    headers = new HashMap<>();
                }

                headers.put(key, value);
                return this;
            }

            public Builder withHeaders(Map<String, String> headers) {

                if (this.headers == null) {
                    this.headers = new HashMap<>();
                }

                this.headers.putAll(headers);
                return this;
            }

            public Endpoint build() {
                return new Endpoint(this.url, this.headers);
            }
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.url);
            dest.writeInt(this.headers.size());
            for (Map.Entry<String, String> entry : this.headers.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }

        protected Endpoint(Parcel in) {
            this.url = in.readString();
            int headersSize = in.readInt();
            this.headers = new HashMap<String, String>(headersSize);
            for (int i = 0; i < headersSize; i++) {
                String key = in.readString();
                String value = in.readString();
                this.headers.put(key, value);
            }
        }

        public static final Creator<Endpoint> CREATOR = new Creator<Endpoint>() {
            @Override
            public Endpoint createFromParcel(Parcel source) {
                return new Endpoint(source);
            }

            @Override
            public Endpoint[] newArray(int size) {
                return new Endpoint[size];
            }
        };

        @Override
        public String toString() {
            return "{url:" + url + "}";
        }
    }
}
