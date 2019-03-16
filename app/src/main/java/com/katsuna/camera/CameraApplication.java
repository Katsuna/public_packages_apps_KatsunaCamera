package com.katsuna.camera;

import android.app.Application;

import timber.log.Timber;

public class CameraApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
