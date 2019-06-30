package com.katsuna.camera;

import android.hardware.camera2.CameraCharacteristics;

import com.katsuna.camera.utils.OrientationManager;
import com.katsuna.commons.entities.UserProfile;

public interface ICameraHost {

    void switchMode(CameraMode cameraMode);

    CameraMode getActiveCameraMode();

    void goToGalleryApp(CameraMode mode);

    String getActiveCameraId();

    void setActiveCameraId(String cameraId);

    CameraCharacteristics getActiveCameraCharacteristics();

    void setActiveCameraCharacteristics(CameraCharacteristics characteristics);

    void showSettings(boolean enabled);

    void lockScreenRotation(boolean flag);

    UserProfile getUserProfile();

    OrientationManager getOrientationManager();
}
