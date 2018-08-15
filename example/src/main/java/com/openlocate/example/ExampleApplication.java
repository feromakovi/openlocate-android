package com.openlocate.example;

import android.app.Application;
import android.os.Build;

import com.openlocate.android.core.OpenLocate;

import java.util.ArrayList;

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ArrayList<OpenLocate.Endpoint> endpoints = new ArrayList<>();

        endpoints.add(OpenLocate.Endpoint.builder(BuildConfig.URL)
                .withHeader("Authorization", "Bearer " + BuildConfig.TOKEN)
                .build());

        OpenLocate.Configuration configuration = new OpenLocate.Configuration.Builder(this, endpoints)
                .withoutDeviceManufacturer()
                .withoutDeviceModel()
                .build();

        OpenLocate.initialize(configuration);
    }
}
