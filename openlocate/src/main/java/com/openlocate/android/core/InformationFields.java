package com.openlocate.android.core;

final class InformationFields {

    private final String manufacturer;
    private final String model;
    private final String operatingSystem;
    private final String isCharging;

    private final String carrierName;
    private final String wifiSsid;
    private final String wifiBssid;
    private final String connectionType;
    private final String locationProvider;

    private InformationFields(String deviceManufacturer, String deviceModel,
                             String chargingState, String operatingSystem,
                             String carrierName, String wifiSSID,
                             String wifiBSSID, String connectionType,
                             String locationMethod) {

            this.manufacturer = deviceManufacturer;
            this.model = deviceModel;
            this.operatingSystem = operatingSystem;
            this.isCharging = chargingState;
            this.carrierName = carrierName;
            this.wifiSsid = wifiSSID;
            this.wifiBssid = wifiBSSID;
            this.connectionType = connectionType;
            this.locationProvider = locationMethod;

    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String isCharging() {
        return isCharging;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public String getWifiSsid() {
        return wifiSsid;
    }

    public String getWifiBssid() {
        return wifiBssid;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public String getLocationProvider() {
        return locationProvider;
    }

    static InformationFields from(String deviceManufacturer,
                                         String deviceModel,
                                         String chargingState,
                                         String operatingSystem,
                                         String carrierName,
                                         String wifiSSID,
                                         String wifiBSSID,
                                         String connectionType,
                                         String locationMethod) {
        return new InformationFields(deviceManufacturer, deviceModel, chargingState, operatingSystem, carrierName, wifiSSID, wifiBSSID, connectionType, locationMethod);
    }

    @Override
    public String toString() {
        return "InformationFields{" +
                "manufacturer='" + manufacturer + '\'' +
                ", model='" + model + '\'' +
                ", operatingSystem='" + operatingSystem + '\'' +
                ", isCharging=" + isCharging +
                ", carrierName='" + carrierName + '\'' +
                ", wifiSsid='" + wifiSsid + '\'' +
                ", wifiBssid='" + wifiBssid + '\'' +
                ", connectionType='" + connectionType + '\'' +
                ", locationProvider=" + locationProvider +
                '}';
    }
}
