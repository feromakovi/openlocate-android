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

import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HttpTaskTests {

    @Test
    public void testHttpTask() {
        // Given
        try {
            JSONObject object = new JSONObject();
            object.put("Test", "value");
            HashMap<String, String> headers = new HashMap<>();
            headers.put("header", "value");

            HttpRequest request = new HttpRequest.Builder()
                    .setUrl("hhttps://posttestserver.com/post.php")
                    .setMethodType(HttpMethodType.POST)
                    .setParams(object.toString())
                    .setAdditionalHeaders(headers)
                    .build();
            HttpTask task = new HttpTask();

            // When
            try {
                HttpResponse response = task.execute(request);

                // Then
                assertEquals(0, response.getStatusCode());
            } catch (Exception e) {
                assertFalse(true);
            }
        } catch (Exception e) {
            assertFalse(true);
        }
    }

}
