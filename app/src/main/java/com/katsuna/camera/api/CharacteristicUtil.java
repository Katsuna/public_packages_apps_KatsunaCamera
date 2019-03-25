package com.katsuna.camera.api;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.util.Size;

import java.util.Arrays;
import java.util.List;

import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE;
import static android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL;
import static android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_AUTO;
import static android.hardware.camera2.CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3;
import static android.hardware.camera2.CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL;
import static android.hardware.camera2.CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED;

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
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
            return true;
        }
        return false;
    }

}
