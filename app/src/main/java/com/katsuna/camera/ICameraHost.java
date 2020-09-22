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
