package com.katsuna.camera.utils;

import android.content.Context;

import com.katsuna.camera.data.source.SettingsDataSource;
import com.katsuna.camera.data.source.SettingsPreferenceDataSource;

public class DepedencyUtils {

    public static SettingsDataSource getSettingsDatasource(Context context) {
        return new SettingsPreferenceDataSource(context);
    }

}
