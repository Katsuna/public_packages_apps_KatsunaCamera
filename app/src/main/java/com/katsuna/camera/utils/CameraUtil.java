package com.katsuna.camera.utils;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

@SuppressWarnings("ALL")
public class CameraUtil {

    public static int getDisplayRotation() {
        WindowManager windowManager = AndroidServices.instance().provideWindowManager();
        int rotation = windowManager.getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    /**
     * Given the device orientation and Camera2 characteristics, this returns
     * the required JPEG rotation for this camera.
     *
     * @param deviceOrientationDegrees the clockwise angle of the device orientation from its
     *                                 natural orientation in degrees.
     * @return The angle to rotate image clockwise in degrees. It should be 0, 90, 180, or 270.
     */
    public static int getJpegRotation(int deviceOrientationDegrees,
                                      CameraCharacteristics characteristics) {
        if (deviceOrientationDegrees == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return 0;
        }
        boolean isFrontCamera = characteristics.get(CameraCharacteristics.LENS_FACING) ==
                CameraMetadata.LENS_FACING_FRONT;
        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        return getImageRotation(sensorOrientation, deviceOrientationDegrees, isFrontCamera);
    }

    /**
     * Given the camera sensor orientation and device orientation, this returns a clockwise angle
     * which the final image needs to be rotated to be upright on the device screen.
     *
     * @param sensorOrientation Clockwise angle through which the output image needs to be rotated
     *                          to be upright on the device screen in its native orientation.
     * @param deviceOrientation Clockwise angle of the device orientation from its
     *                          native orientation when front camera faces user.
     * @param isFrontCamera True if the camera is front-facing.
     * @return The angle to rotate image clockwise in degrees. It should be 0, 90, 180, or 270.
     */
    public static int getImageRotation(int sensorOrientation,
                                       int deviceOrientation,
                                       boolean isFrontCamera) {
        // The sensor of front camera faces in the opposite direction from back camera.
        if (isFrontCamera) {
            deviceOrientation = (360 - deviceOrientation) % 360;
        }
        return (sensorOrientation + deviceOrientation) % 360;
    }
}
