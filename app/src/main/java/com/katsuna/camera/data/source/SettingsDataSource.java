package com.katsuna.camera.data.source;

import android.support.annotation.NonNull;

import com.katsuna.camera.data.BlackAndWhiteMode;
import com.katsuna.camera.data.FlashMode;
import com.katsuna.camera.data.SizeMode;

public interface SettingsDataSource {

    @NonNull
    FlashMode getFlashMode();

    void setFlashMode(@NonNull FlashMode flashMode);

    @NonNull
    SizeMode getSizeMode();

    void setSizeMode(@NonNull SizeMode sizeMode);

    @NonNull
    BlackAndWhiteMode getBlackAndWhiteMode();

    void setBlackAndWhiteMode(@NonNull BlackAndWhiteMode blackAndWhiteMode);

}
