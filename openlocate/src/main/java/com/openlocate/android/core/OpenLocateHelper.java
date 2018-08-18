package com.openlocate.android.core;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;

import java.util.List;

import static com.firebase.jobdispatcher.RetryStrategy.RETRY_POLICY_EXPONENTIAL;

final class OpenLocateHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = OpenLocateHelper.class.getSimpleName();
    private final static String LOCATION_DISPATCH_TAG = OpenLocate.class.getCanonicalName() + ".location_dispatch_task_v2";

    private final Context context;

    private OpenLocate.Configuration configuration;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private FirebaseJobDispatcher jobDispatcher;

    public OpenLocateHelper(Context context, OpenLocate.Configuration configuration) {
        this.context = context;
        this.configuration = configuration;

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
    }

    public void startTracking() {
        if (mGoogleApiClient.isConnected() == false && mGoogleApiClient.isConnecting() == false) {
            mGoogleApiClient.connect();
        }
    }

    public void stopTracking() {
        stopLocationCollection();
        mGoogleApiClient.disconnect();
    }

    public void updateConfiguration(OpenLocate.Configuration configuration) {
        this.configuration = configuration;
        if (mGoogleApiClient.isConnected()) {
            stopLocationCollection();
            startLocationCollection();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationCollection();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "Connection suspended. Error code: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        final String text = "Exception while connecting to Google Play Services";
        Log.w(TAG, text + ": " + connectionResult.getErrorMessage());
    }

    private void startLocationCollection() {
        buildLocationRequest();
        requestLocationUpdates();
        scheduleDispatchLocationService();
    }

    private void stopLocationCollection() {
        removeLocationUpdates();
        unscheduleDispatchLocationService();
    }

    private void buildLocationRequest() {
        long locationUpdateInterval = configuration.getLocationUpdateInterval() * 1000;
        long locationTransmissionInterval = configuration.getTransmissionInterval() * 1000;
        int locationAccuracy = configuration.getLocationAccuracy().getLocationRequestAccuracy();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(locationUpdateInterval);
        mLocationRequest.setFastestInterval(locationUpdateInterval / 2);
        mLocationRequest.setMaxWaitTime(Math.max(locationUpdateInterval * 2, (int)(locationTransmissionInterval * 0.85 / 3)));
        mLocationRequest.setPriority(locationAccuracy);
    }

    private void requestLocationUpdates() {
        try {
            Log.i(TAG, "Starting OL Updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, getLocationUpdatePendingIntent());
        } catch (SecurityException e) {
            Log.e(TAG, "Could not start OL Updates");
        }
    }

    private void removeLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, getLocationUpdatePendingIntent());
        }
    }

    private PendingIntent getLocationUpdatePendingIntent() {
        Intent intent = new Intent(context, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void scheduleDispatchLocationService() {
        List<OpenLocate.Endpoint> endpoints = configuration.getEndpoints();
        if (endpoints == null || jobDispatcher == null) {
            return;
        }

        Bundle bundle = new Bundle();
        try {
            bundle.putString(Constants.ENDPOINTS_KEY, OpenLocate.Endpoint.toJson(endpoints));
        } catch (JSONException e) {
            e.printStackTrace();
            stopTracking();
        }

        long transmissionIntervalInSecs = configuration.getTransmissionInterval();

        int initialBackoff = 600;
        int maximumBackoff = Math.max((int)transmissionIntervalInSecs / 2, 3600);

        Job job = jobDispatcher.newJobBuilder()
                .setService(DispatchLocationService.class)
                .setTag(LOCATION_DISPATCH_TAG)
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setTrigger(Trigger.executionWindow((int)(transmissionIntervalInSecs * 0.9), (int)(transmissionIntervalInSecs * 1.1)))
                .setReplaceCurrent(false)
                .setRetryStrategy(jobDispatcher.newRetryStrategy(RETRY_POLICY_EXPONENTIAL, initialBackoff, maximumBackoff))
                .setExtras(bundle)
                .build();

        try {
            jobDispatcher.mustSchedule(job);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Google Play Services is not up to date.");
            stopTracking();
        }
    }

    private void unscheduleDispatchLocationService() {
        if (jobDispatcher != null) {
            jobDispatcher.cancel(LOCATION_DISPATCH_TAG);
        }
    }

}
