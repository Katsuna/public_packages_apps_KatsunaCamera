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
