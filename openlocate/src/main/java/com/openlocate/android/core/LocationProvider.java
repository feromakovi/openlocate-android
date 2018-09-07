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
import android.location.LocationManager;

import java.util.HashMap;
import java.util.Map;

enum LocationProvider {
    GPS("gps"),
    NETWORK("network"),
    PASSIVE("passive"),
    DISABLED("disabled");

    private final String value;

    private static final Map<String, LocationProvider>  lookup  = new HashMap<>();

    static {
        for (LocationProvider provider: LocationProvider.values()) {
                lookup.put(provider.getValue(), provider);
        }
    }

    LocationProvider(final String value) {
        this.value = value;
    }

    String getValue() {
       return value;
    }

    public static LocationProvider get(String value) {
        return lookup.get(value);
    }

//    static LocationProvider getLocationProvider(Context context) {
//        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            return GPS;
//        }
//
//        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            return NETWORK;
//        }
//
//        if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
//            return PASSIVE;
//        }
//
//        return DISABLED;
//    }
}
