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

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

final class LocationDispatcher {

    private static final String TAG = LocationDispatcher.class.getSimpleName();
    private static final String LOCATIONS_KEY = "locations";

    List<OpenLocateLocation> postLocations(HttpClient httpClient, final OpenLocate.Endpoint endpoint, String userAgent, long sinceId, final LocationDataSource dataSource) {
        final List<OpenLocateLocation> locations = dataSource.getSince(sinceId);

        if (locations == null || locations.isEmpty()) {
            Log.i(TAG, "Attempted to post locations, but found none to post.");
            return null;
        }

        final String endpointUrl = endpoint.getUrl();
        httpClient.post(
                endpointUrl,
                getLocationsParam(locations).toString(),
                getHeaders(endpoint, userAgent),
                new HttpClientCallback() {
                    @Override
                    public void onCompletion(HttpRequest request, HttpResponse response) {
                        Log.i(TAG, "Successfully posted locations to " + endpointUrl);
                    }
                }, new HttpClientCallback() {
                    @Override
                    public void onCompletion(HttpRequest request, HttpResponse response) {
                        locations.clear();
                        Log.e(TAG, "Fail to post location to " + endpointUrl);
                    }
                }
        );

        dataSource.close();

        return locations;
    }

    @NonNull
    private HashMap getHeaders(OpenLocate.Endpoint endpoint, String userAgent) {
        HashMap headers = endpoint.getHeaders();
        if (headers == null) {
            headers = new HashMap();
        }
        if (headers.containsKey("User-Agent") == false) {
            headers.put("User-Agent", userAgent);
        }
        headers.put("x-client", "App/1.0.26 ol-android/2.0.0");
        return headers;
    }

    private JSONObject getLocationsParam(List<OpenLocateLocation> locationsToPost) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (OpenLocateLocation location : locationsToPost) {
            jsonArray.put(location.getJson());
        }

        try {
            jsonObject.put(LOCATIONS_KEY, jsonArray);
        } catch (JSONException e) {
            Log.e(TAG, "JSON exception while posting locations " + e.getMessage());
        }

        return jsonObject;
    }
}
