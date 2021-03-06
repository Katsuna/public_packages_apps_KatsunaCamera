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
package com.katsuna.camera.api;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.util.Size;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE;
import static android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL;
import static android.hardware.camera2.CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES;
import static android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP;
import static android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_AUTO;
import static android.hardware.camera2.CameraMetadata.CONTROL_EFFECT_MODE_MONO;
import static android.hardware.camera2.CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3;
import static android.hardware.camera2.CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL;
import static android.hardware.camera2.CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED;
import static android.hardware.camera2.CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING;
import static android.hardware.camera2.CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING;

public class CharacteristicUtil {

    private static boolean isSupported(@NonNull CameraCharacteristics receiver,
                                       @NonNull CameraCharacteristics.Key<int[]> modes,
                                       int mode) {

        int[] modesAvailable = receiver.get(modes);
        if (modesAvailable == null) return false;

        for (int m : modesAvailable) {
            if (m == mode) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFlashSupported(@NonNull CameraCharacteristics r) {
        Boolean available = r.get(FLASH_INFO_AVAILABLE);
        return available == null ? false : available;
    }

    public static boolean isAutoExposureSupported(@NonNull CameraCharacteristics r, int mode) {
        return isSupported(r, CONTROL_AE_AVAILABLE_MODES, mode);
    }

    public static boolean isContinuousAutoFocusSupported(@NonNull CameraCharacteristics r) {
        return isSupported(r, CONTROL_AF_AVAILABLE_MODES, CONTROL_AF_MODE_CONTINUOUS_PICTURE);
    }

    public static boolean isAutoWhiteBalanceSupported(@NonNull CameraCharacteristics r) {
        return isSupported(r, CONTROL_AWB_AVAILABLE_MODES, CONTROL_AWB_MODE_AUTO);
    }

    @NonNull
    public static List<Size> getCaptureOutputSizes(@NonNull CameraCharacteristics characteristics) {
        StreamConfigurationMap map = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP);

        if (map == null) {
            throw new NullPointerException("not properly supported camera2 api");
        }

        return Arrays.asList(map.getOutputSizes(ImageFormat.JPEG));
    }

    @NonNull
    public static List<Size> getPreviewOutputSizes(@NonNull CameraCharacteristics characteristics) {
        StreamConfigurationMap map = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP);

        if (map == null) {
            throw new NullPointerException("not properly supported camera2 api");
        }

        return Arrays.asList(map.getOutputSizes(SurfaceTexture.class));
    }

    public static boolean camera2Supported(@NonNull CameraCharacteristics characteristics) {
        boolean output = false;

        Integer supportedLevel = characteristics.get(INFO_SUPPORTED_HARDWARE_LEVEL);
        if (supportedLevel != null) {
            output = (supportedLevel == INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
                    || supportedLevel == INFO_SUPPORTED_HARDWARE_LEVEL_FULL
                    || supportedLevel == INFO_SUPPORTED_HARDWARE_LEVEL_3);
        }
        return output;
    }

    public static boolean isLensFacing(@NonNull CameraCharacteristics characteristics) {
        // Does the camera have a forwards facing lens?
        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        return facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT;
    }

    public static boolean isZeroShutterLagSupported(@NonNull CameraCharacteristics c) {
        boolean cond1 = isSupported(c, REQUEST_AVAILABLE_CAPABILITIES,
                REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING);

        boolean cond2 = isSupported(c, REQUEST_AVAILABLE_CAPABILITIES,
                REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING);

        boolean output = cond1 || cond2;

        Timber.d("isZeroShutterLagSupported: %s", output);

        return output;
    }

    public static boolean isBWColorModeSupported(@NonNull CameraCharacteristics r) {
        boolean output = isSupported(r, CONTROL_AVAILABLE_EFFECTS, CONTROL_EFFECT_MODE_MONO);

        Timber.d("isBWColorModeSupported: %s", output);

        return output;
    }

    public static boolean isMeteringAreaAFSupported(@NonNull CameraCharacteristics r) {
        Integer maxAfRegions = r.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
        return maxAfRegions != null && maxAfRegions >= 1;
    }

    public static Rect getSensorArraySize(@NonNull CameraCharacteristics r) {
        return r.get(SENSOR_INFO_ACTIVE_ARRAY_SIZE);
    }

}
