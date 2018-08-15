
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
package com.openlocate.example.fragments;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.openlocate.android.core.OpenLocate;
import com.openlocate.example.BuildConfig;
import com.openlocate.example.R;
import com.openlocate.example.activities.MainActivity;

public class TrackFragment extends Fragment {

    private static String TAG = MainActivity.class.getSimpleName();

    private Activity activity;
    private Button startButton;
    private Button stopButton;
    private TextView versionLabel;

    public static TrackFragment getInstance() {
        return new TrackFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track, null);

        startButton = (Button) view.findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTracking();
            }
        });

        stopButton = (Button) view.findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTracking();
            }
        });

        versionLabel = (TextView) view.findViewById(R.id.version_label);
        configureVersionLabel();

        onTrackingStatusChange();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.app_name);
    }

    private void startTracking() {
        OpenLocate.getInstance().startTracking(getActivity());
        onTrackingStatusChange();
    }

    private void stopTracking() {
        OpenLocate.getInstance().stopTracking();
        onTrackingStatusChange();
    }

    private void onTrackingStatusChange() {
        boolean enabled = OpenLocate.getInstance().isTracking();

        startButton.setVisibility(enabled ? View.GONE : View.VISIBLE);
        stopButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    private void configureVersionLabel() {
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            String version = packageInfo.versionName;
            int verCode = packageInfo.versionCode;

            versionLabel.setText("v" + version + "." + verCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}
