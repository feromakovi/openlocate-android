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

import android.location.Location;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public final class OpenLocateLocation implements JsonObjectType {

    class Keys {
        static final String LATITUDE = "latitude";
        static final String LONGITUDE = "longitude";
        static final String HORIZONTAL_ACCURACY = "horizontal_accuracy";
        static final String TIMESTAMP = "utc_timestamp";
        static final String TIMESTAMP_RECEIVED = "utc_timestamp_received";
        static final String TIMEZONE = "timezone";
        static final String AD_ID = "ad_id";
        static final String AD_OPT_OUT = "ad_opt_out";
        static final String AD_TYPE = "id_type";

        static final String COURSE = "course";
        static final String SPEED = "speed";
        static final String ALTITUDE = "altitude";
        static final String VERTICAL_ACCURACY = "vertical_accuracy";

        static final String IS_CHARGING = "is_charging";
        static final String DEVICE_MANUFACTURER = "device_manufacturer";
        static final String DEVICE_MODEL = "device_model";
        static final String OPERATING_SYSTEM = "os_version";

        static final String LOCATION_METHOD = "location_method";
        static final String LOCATION_CONTEXT = "location_context";

        static final String CARRIER_NAME = "carrier_name";
        static final String CONNECTION_TYPE = "connection_type";
        static final String WIFI_SSID = "wifi_ssid";
        static final String WIFI_BSSID = "wifi_bssid";
    }

    private static final String ADVERTISING_ID_TYPE = "aaid";

    private Date created;
    private String timezone;
    private LocationInfo location;
    private AdvertisingIdClient.Info advertisingInfo;
    private InformationFields informationFields;

    public Date getCreated() {
        return created;
    }

    public LocationInfo getLocation() {
        return location;
    }

    public void setLocation(LocationInfo location) {
        this.location = location;
    }

    public static OpenLocateLocation from(Location location,
                                          AdvertisingIdClient.Info advertisingInfo, InformationFields informationFields) {
        return new OpenLocateLocation(location, advertisingInfo, informationFields);
    }

    OpenLocateLocation(
            Location location,
            AdvertisingIdClient.Info advertisingInfo, InformationFields informationFields) {
        this.location = new LocationInfo(location);
        this.advertisingInfo = advertisingInfo;
        this.informationFields = informationFields;
        this.created = new Date();
        this.timezone = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT).replace("GMT", "").replace(":", "");
    }

    OpenLocateLocation(Date created, String jsonString) {
        this.created = created;
        try {
            JSONObject json = new JSONObject(jsonString);

            location = new LocationInfo();
            location.setLatitude(json.getDouble(Keys.LATITUDE));
            location.setLongitude(json.getDouble(Keys.LONGITUDE));
            location.setHorizontalAccuracy(Float.parseFloat(json.getString(Keys.HORIZONTAL_ACCURACY)));
            location.setTimeStampSecs(json.getLong(Keys.TIMESTAMP));
            location.setAltitude(json.getDouble(Keys.ALTITUDE));
            location.setCourse(Float.parseFloat(json.getString(Keys.COURSE)));
            location.setSpeed(Float.parseFloat(json.getString(Keys.SPEED)));

            try {
                location.setVerticalAccuracy(Float.parseFloat(json.getString(Keys.VERTICAL_ACCURACY)));
            } catch (JSONException e) {
                location.setVerticalAccuracy(0);
            }

            String deviceManufacturer = "";
            if (json.has(Keys.DEVICE_MANUFACTURER)) {
                deviceManufacturer = json.getString(Keys.DEVICE_MANUFACTURER);
            }


            String deviceModel = "";
            if (json.has(Keys.DEVICE_MODEL)) {
                deviceModel = json.getString(Keys.DEVICE_MODEL);
            }

            String chargingState = "";
            if (json.has(Keys.IS_CHARGING)) {
                chargingState = json.getString(Keys.IS_CHARGING);
            }

            String operatingSystem = "";
            if (json.has(Keys.OPERATING_SYSTEM)) {
                operatingSystem = json.getString(Keys.OPERATING_SYSTEM);
            }

            String carrierName = "";
            if (json.has(Keys.CARRIER_NAME)) {
                carrierName = json.getString(Keys.CARRIER_NAME);
            }

            String wifiSSID = "";
            if (json.has(Keys.WIFI_SSID)) {
                wifiSSID = json.getString(Keys.WIFI_SSID);
            }

            String wifiBSSID = "";
            if (json.has(Keys.WIFI_BSSID)) {
                wifiBSSID = json.getString(Keys.WIFI_BSSID);
            }

            String connectionType = "";
            if (json.has(Keys.CONNECTION_TYPE)) {
                connectionType = json.getString(Keys.CONNECTION_TYPE);
            }

            String locationMethod = "";
            if (json.has(Keys.LOCATION_METHOD)) {
                locationMethod = json.getString(Keys.LOCATION_METHOD);
            }

            timezone = json.has(Keys.TIMEZONE) ? json.getString(Keys.TIMEZONE) : "";

            informationFields = InformationFieldsFactory.getInformationFields(deviceManufacturer, deviceModel, chargingState, operatingSystem, carrierName, wifiSSID, wifiBSSID, connectionType, locationMethod);

            advertisingInfo = new AdvertisingIdClient.Info(
                    json.getString(Keys.AD_ID),
                    json.getBoolean(Keys.AD_OPT_OUT)
            );

        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public JSONObject getJson() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject
                    .put(Keys.LATITUDE, location.getLatitude())
                    .put(Keys.LONGITUDE, location.getLongitude())
                    .put(Keys.HORIZONTAL_ACCURACY, String.valueOf(location.getHorizontalAccuracy()))
                    .put(Keys.TIMESTAMP, location.getTimeStampSecs())
                    .put(Keys.COURSE, String.valueOf(location.getCourse()))
                    .put(Keys.SPEED, String.valueOf(location.getSpeed()))
                    .put(Keys.ALTITUDE, location.getAltitude())
                    .put(Keys.AD_ID, advertisingInfo.getId())
                    .put(Keys.AD_OPT_OUT, advertisingInfo.isLimitAdTrackingEnabled())
                    .put(Keys.AD_TYPE, ADVERTISING_ID_TYPE)
                    .put(Keys.VERTICAL_ACCURACY, location.getVerticalAccuracy())
                    .put(Keys.TIMEZONE, timezone);

            if (created != null) {
                jsonObject.put(Keys.TIMESTAMP_RECEIVED, TimeUnit.MILLISECONDS.toSeconds(getCreated().getTime()));
            }

            if(!TextUtils.isEmpty(informationFields.getManufacturer())) {
                jsonObject.put(Keys.DEVICE_MANUFACTURER, informationFields.getManufacturer());
            }

            if(!TextUtils.isEmpty(informationFields.getModel())) {
                jsonObject.put(Keys.DEVICE_MODEL, informationFields.getModel());
            }

            if(!TextUtils.isEmpty(informationFields.isCharging())) {
                jsonObject.put(Keys.IS_CHARGING, informationFields.isCharging());
            }

            if(!TextUtils.isEmpty(informationFields.getOperatingSystem())) {
                jsonObject.put(Keys.OPERATING_SYSTEM, informationFields.getOperatingSystem());
            }

            if(!TextUtils.isEmpty(informationFields.getCarrierName())) {
                jsonObject.put(Keys.CARRIER_NAME, informationFields.getCarrierName());
            }

            if(!(TextUtils.isEmpty(informationFields.getWifiSsid()) && TextUtils.isEmpty(informationFields.getWifiBssid()))) {
                jsonObject.put(Keys.WIFI_SSID, informationFields.getWifiSsid());
                jsonObject.put(Keys.WIFI_BSSID, informationFields.getWifiBssid());
            }

            if(!TextUtils.isEmpty(informationFields.getConnectionType())) {
                jsonObject.put(Keys.CONNECTION_TYPE, informationFields.getConnectionType());
            }

            if(!TextUtils.isEmpty(informationFields.getLocationProvider())) {
                jsonObject.put(Keys.LOCATION_METHOD, informationFields.getLocationProvider());
            }
        } catch (NullPointerException | JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public class LocationInfo {

        private double latitude;
        private double longitude;
        private float horizontalAccuracy;
        private long timeStampSecs;
        private float speed;
        private float course;
        private double altitude;

        private float verticalAccuracy;

        LocationInfo() {

        }

        LocationInfo(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            horizontalAccuracy = location.getAccuracy();
            timeStampSecs = TimeUnit.MILLISECONDS.toSeconds(getFixTime(location));
            speed = location.getSpeed();
            course = location.getBearing();
            altitude = location.getAltitude();

            if (Build.VERSION.SDK_INT >= 26) {
                verticalAccuracy = location.getVerticalAccuracyMeters();
            }
        }

        public double getLatitude() {
            return latitude;
        }

        void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getHorizontalAccuracy() {
            return horizontalAccuracy;
        }

        void setHorizontalAccuracy(float horizontalAccuracy) {
            this.horizontalAccuracy = horizontalAccuracy;
        }

        long getTimeStampSecs() {
            return timeStampSecs;
        }

        void setTimeStampSecs(long timeStampSecs) {
            this.timeStampSecs = timeStampSecs;
        }

        float getSpeed() {
            return speed;
        }

        void setSpeed(float speed) {
            this.speed = speed;
        }

        float getCourse() {
            return course;
        }

        void setCourse(float course) {
            this.course = course;
        }

        double getAltitude() {
            return altitude;
        }

        void setAltitude(double altitude) {
            this.altitude = altitude;
        }

        public float getVerticalAccuracy() {
            return verticalAccuracy;
        }

        public void setVerticalAccuracy(float verticalAccuracy) {
            this.verticalAccuracy = verticalAccuracy;
        }

        @Override
        public String toString() {
            return "LocationInfo{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", horizontalAccuracy=" + horizontalAccuracy +
                    ", timeStampSecs=" + timeStampSecs +
                    ", speed=" + speed +
                    ", course=" + course +
                    ", altitude=" + altitude +
                    ", verticalAccuracy=" + verticalAccuracy +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "OpenLocateLocation{" +
                "location=" + location +
                ", advertisingInfo=" + advertisingInfo +
                ", informationFields=" + informationFields +
                ", receivedAt=" + created +
                '}';
    }

    static long getFixTime(Location location) {
        final long bootTime = SystemClock.elapsedRealtimeNanos();
        final long diff = bootTime - location.getElapsedRealtimeNanos();
        final long result = System.currentTimeMillis() - TimeUnit.NANOSECONDS.toMillis(diff);

        Log.i(Location.class.getSimpleName(), "============ \n" +
                "Time: " + location.getTime() +
                "\n Location elapsedRealTime: " + TimeUnit.NANOSECONDS.toMillis(location.getElapsedRealtimeNanos()) +
                "\n System bootElapsed: " + TimeUnit.NANOSECONDS.toMillis(bootTime) +
                "\n Diff: " + TimeUnit.NANOSECONDS.toMillis(diff) +
                "\n Current: " + System.currentTimeMillis() +
                "\n Result: " + result);
        return result;
    }
}
