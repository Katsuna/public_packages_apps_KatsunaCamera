package com.katsuna.camera;

import android.hardware.camera2.CameraCharacteristics;

import com.katsuna.commons.entities.UserProfile;

public interface ICameraHost {

    void switchMode(CameraMode cameraMode);

    CameraMode getActiveCameraMode();

    void goToGalleryApp();

    String getActiveCameraId();

    void setActiveCameraId(String cameraId);

    CameraCharacteristics getActiveCameraCharacteristics();

    void setActiveCameraCharacteristics(CameraCharacteristics characteristics);

    void showSettings(boolean enabled);

    void lockScreenRotation(boolean flag);

    UserProfile getUserProfile();
}
