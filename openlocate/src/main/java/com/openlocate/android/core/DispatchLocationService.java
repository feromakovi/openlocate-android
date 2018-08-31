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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.firebase.jobdispatcher.SimpleJobService;
import com.openlocate.android.BuildConfig;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.json.JSONException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

final public class DispatchLocationService extends SimpleJobService {

    private final static String TAG = DispatchLocationService.class.getSimpleName();

    public static final long EXPIRED_PERIOD = TimeUnit.DAYS.toMillis(10);

    @Override
    public int onRunJob(JobParameters job) {
        final InfoManager infoManager = new InfoManager(this);
        List<OpenLocate.Endpoint> endpoints = null;
        try {
            endpoints = OpenLocate.Endpoint.fromJson(job.getExtras().getString(Constants.ENDPOINTS_KEY));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int syncedCount = -1;
        try {
            syncedCount = sendLocations(this, endpoints);
            Log.i(TAG, "DispatchLocation success: " + syncedCount);
            infoManager.sync(syncedCount);
        }
        catch (RuntimeException e) {
            Log.e(TAG, "Could not persist ol updates.");
        } finally {
            if (syncedCount > 0) {
                return RESULT_SUCCESS;
            }
            return RESULT_FAIL_RETRY;
        }
    }

    public static int sendLocations(Context context, List<OpenLocate.Endpoint> endpoints) {

        int syncedCount = -1;

        SQLiteOpenHelper helper = DatabaseHelper.getInstance(context);
        LocationDataSource dataSource = new LocationDatabase(helper);
        HttpClient httpClient = new HttpClientImpl();

        LocationDispatcher dispatcher = new LocationDispatcher();
        String userAgent = getUserAgent(context);
        List<Long> timestamps = new ArrayList<>(endpoints.size());
        for (OpenLocate.Endpoint endpoint : endpoints) {

            String key = md5(endpoint.getUrl().toLowerCase());

            try {
                long timestamp = SharedPreferenceUtils.getInstance(context).getLongValue(key, 0);
                List<OpenLocateLocation> sentLocations = dispatcher.postLocations(httpClient, endpoint, userAgent, timestamp, dataSource);

                if (sentLocations != null && !sentLocations.isEmpty()) {
                    syncedCount = sentLocations.size();
                    long latestCreatedLocationDate =
                            sentLocations.get(sentLocations.size() - 1).getCreated().getTime();
                    SharedPreferenceUtils.getInstance(context).setValue(key, latestCreatedLocationDate);
                } else if (sentLocations != null && sentLocations.isEmpty()) {
                    syncedCount = -1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            timestamps.add(SharedPreferenceUtils.getInstance(context).getLongValue(key, 0));
        }

        Long min = Collections.min(timestamps);
        if (min != null) {
            long expired = System.currentTimeMillis() - EXPIRED_PERIOD;

            if (min < expired) {
                min = expired;
            }

            try {
                dataSource.deleteBefore(min);
            } catch (SQLiteFullException exception) {
                Log.w(TAG, "Database is full. Cannot purge data.");
            } finally {
                dataSource.close();
            }
        }

        return syncedCount;
    }

    //public static boolean sendLocations(Context context) throws JSONException {
    //    return sendLocations(context, getEndpoints(context));
    //}

    public static List<OpenLocate.Endpoint> getEndpoints(Context context) throws JSONException {
        SharedPreferenceUtils preferences = SharedPreferenceUtils.getInstance(context);
        String json = preferences.getStringValue(Constants.ENDPOINTS_KEY, "");
        return OpenLocate.Endpoint.fromJson(json);
    }

    private static String md5(String in) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return in;
    }

    private static String getUserAgent(Context context) {
        String appName = getApplicationName(context);
        String appVersion = "N/A";
        int appVersionCode = 0;
        String appPackageName;
        String osVersion = android.os.Build.VERSION.RELEASE;
        String deviceName = Build.MODEL;
        String sdkVersion = BuildConfig.VERSION_NAME;

        try {
            PackageManager packageManager = context.getPackageManager();
            appPackageName = context.getPackageName();
            PackageInfo packageInfo = packageManager.getPackageInfo(appPackageName, 0);
            if (packageInfo != null) {
                appVersion = packageInfo.versionName;
                appVersionCode = packageInfo.versionCode;
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            return "OpenLocate";
        }

        return appName + "/" + appVersion + " (" + appPackageName + "; build:" + appVersionCode +
                "; Android " + osVersion + "; " + deviceName + ") OpenLocate/" + sdkVersion;
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }
}
