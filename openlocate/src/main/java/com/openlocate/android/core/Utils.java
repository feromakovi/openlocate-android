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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

final class Utils {

    static HashMap<String, String> hashMapFromString(String mapString) {
        if (mapString == null || mapString.isEmpty()) {
            return null;
        }

        Properties properties = new Properties();
        try {
            properties.load(new StringReader(
                    mapString.substring(1, mapString.length() - 1).replace(", ", "\n"))
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<Object, Object> entry: properties.entrySet()) {
            map.put((String)entry.getKey(), (String)entry.getValue());
        }

        return map;
    }

    public static String formatTimeZoneOffset() {
        final int rawOffset = java.util.TimeZone.getDefault().getRawOffset();
        if (rawOffset == 0) {
            return "+0000";
        }
        long hours = TimeUnit.MILLISECONDS.toHours(rawOffset);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(rawOffset);
        minutes = Math.abs(minutes - TimeUnit.HOURS.toMinutes(hours));

        return String.format("%+03d%02d", hours, Math.abs(minutes));
    }
}
