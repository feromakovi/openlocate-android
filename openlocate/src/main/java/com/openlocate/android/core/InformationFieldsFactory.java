package com.openlocate.android.core;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;

final class InformationFieldsFactory {

    private Context context;
    private OpenLocate.Configuration configuration;

    private String manufacturer;
    private String model;
    private String operatingSystem;
    private String isCharging;

    private String carrierName;
    private String wifiSsid;
    private String wifiBssid;
    private String connectionType;
    private LocationProvider locationProvider;
    private LocationContext locationContext;

    private static final String BASE_NAME = "Android";

    private InformationFieldsFactory(Context context, OpenLocate.Configuration configuration) {

        if (configuration == null) {
            return;
        }

        this.configuration = configuration;
        this.context = context;

        updateDeviceInfo();

        if (!this.configuration.isCarrierNameCollectionDisabled()) {
            updateCarrierName();
        }

        if (!this.configuration.isWifiCollectionDisabled()) {
            updateWifiInfo();
        }

        if (!this.configuration.isConnectionTypeCollectionDisabled()) {
            updateConnectionType();
        }

        if (!this.configuration.isLocationMethodCollectionDisabled()) {
            updateLocationProvider();
        }
        if (!this.configuration.isLocationContextCollectionDisabled()) {
            updateLocationContext();
        }

    }

    public static InformationFields collectInformationFields(Context context, OpenLocate.Configuration configuration) {

        InformationFieldsFactory informationFieldsFactory =  new InformationFieldsFactory(context, configuration);

        return InformationFields.from(informationFieldsFactory.manufacturer, informationFieldsFactory.model, informationFieldsFactory.isCharging,
                informationFieldsFactory.operatingSystem, informationFieldsFactory.carrierName, informationFieldsFactory.wifiSsid,
                informationFieldsFactory.wifiBssid, informationFieldsFactory.connectionType, informationFieldsFactory.locationProvider.getValue(), informationFieldsFactory.locationContext.getValue());

    }

    public static InformationFields getInformationFields(String deviceManufacturer, String deviceModel, String chargingState,
                                                         String operatingSystem, String carrierName, String wifiSSID,
                                                         String wifiBSSID, String connectionType, String locationMethod, String locationContext) {

        return InformationFields.from(deviceManufacturer, deviceModel, chargingState,
                                      operatingSystem, carrierName, wifiSSID,
                                      wifiBSSID, connectionType, locationMethod, locationContext);
    }

    private void updateDeviceInfo() {

        if (!configuration.isDeviceManufacturerCollectionDisabled()) {
            this.manufacturer = Build.MANUFACTURER;
        }

        if (!configuration.isDeviceModelCollectionDisabled()) {
            this.model = Build.MODEL;
        }

        if (!configuration.isOperaringSystemCollectionDisbaled()) {
            this.operatingSystem = BASE_NAME + " " + Build.VERSION.RELEASE;
        }

        if (!configuration.isChargingInfoCollectionDisabled()) {
            Intent batteryIntent = context.getApplicationContext().registerReceiver(
                    null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            );
            this.isCharging = String.valueOf(isDeviceCharging(batteryIntent));
        }
    }

    private void updateLocationContext() {
        locationContext = LocationContext.getLocationContext();
    }

    private void updateLocationProvider() {
        locationProvider = LocationProvider.getLocationProvider(context);
    }

    private boolean isDeviceCharging(Intent batteryIntent) {
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        return status == BatteryManager.BATTERY_STATUS_CHARGING;
    }

    private void updateCarrierName() {
        TelephonyManager telephonyManager = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null ) {
            carrierName = telephonyManager.getNetworkOperatorName();
        }
    }

    private void updateWifiInfo() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if (wifiInfo != null) {
                wifiSsid = wifiInfo.getSSID();
                wifiBssid = wifiInfo.getBSSID();
            };
        }
    }

    private void updateConnectionType() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            connectionType = "none";
            return;
        }

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || networkInfo.isConnected() == false) {
            connectionType = "none";
            return;
        }

        int type = networkInfo.getType();
        switch (type) {
            case ConnectivityManager.TYPE_WIFI:
                connectionType = "wifi";
                break;
            case ConnectivityManager.TYPE_MOBILE:
                connectionType = "cellular";
                break;
            default:
                connectionType = "unknown";
        }
    }
}
