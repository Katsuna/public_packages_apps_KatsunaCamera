package com.katsuna.camera;

import android.app.Application;
import android.content.Context;

import com.katsuna.camera.utils.AndroidContext;

import timber.log.Timber;

public class CameraApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Android context must be the first item initialized.
        Context context = getApplicationContext();
        AndroidContext.initialize(context);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
