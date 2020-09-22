/**
* Copyright (C) 2020 Manos Saratsis
*
* This file is part of Katsuna.
*
* Katsuna is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Katsuna is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Katsuna.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.katsuna.camera.data.source;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.katsuna.camera.data.BlackAndWhiteMode;
import com.katsuna.camera.data.FlashMode;
import com.katsuna.camera.data.SizeMode;

public class SettingsPreferenceDataSource implements SettingsDataSource {

    private static final String FLASH_MODE = "FLASH_MODE";
    private static final String SIZE_MODE = "SIZE_MODE";
    private static final String BW_MODE = "BW_MODE";

    private final SharedPreferences mSharedPrefs;
    private final Context mContext;

    public SettingsPreferenceDataSource(Context context) {
        mContext = context;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private void saveWeather(@NonNull String key, @NonNull String weather) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(key, weather);
        editor.apply();
    }

    @NonNull
    @Override
    public FlashMode getFlashMode() {
        String modeStr = mSharedPrefs.getString(FLASH_MODE, FlashMode.defaultMode().toString());
        return FlashMode.toEnum(modeStr);
    }

    @Override
    public void setFlashMode(@NonNull FlashMode flashMode) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(FLASH_MODE, flashMode.toString());
        editor.apply();
    }

    @NonNull
    @Override
    public SizeMode getSizeMode() {
        String modeStr = mSharedPrefs.getString(SIZE_MODE, SizeMode.defaultMode().toString());
        return SizeMode.toEnum(modeStr);
    }

    @Override
    public void setSizeMode(@NonNull SizeMode sizeMode) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(SIZE_MODE, sizeMode.toString());
        editor.apply();
    }

    @NonNull
    @Override
    public BlackAndWhiteMode getBlackAndWhiteMode() {
        String modeStr = mSharedPrefs.getString(BW_MODE, BlackAndWhiteMode.defaultMode().toString());
        return BlackAndWhiteMode.toEnum(modeStr);
    }

    @Override
    public void setBlackAndWhiteMode(@NonNull BlackAndWhiteMode blackAndWhiteMode) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(BW_MODE, blackAndWhiteMode.toString());
        editor.apply();
    }
}
