package com.katsuna.camera;

import android.Manifest;

final class Constants {

    static final int REQUEST_CAMERA_PERMISSION = 1;
    static final int REQUEST_VIDEO_PERMISSIONS = 2;

    static final String[] CAMERA_PERMISSIONS = {Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE};

    static final String[] VIDEO_PERMISSIONS = {
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

}
