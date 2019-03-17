package com.katsuna.camera.api;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import timber.log.Timber;

public class CameraHelper {

    private final Context mContext;
    private final CameraManager mCameraManager;

    public CameraHelper(@NonNull Context context) {
        mContext = context;
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
    }

    public String[] getCameraIdList() throws CameraAccessException {
        return mCameraManager.getCameraIdList();
    }

    public CameraCharacteristics getCameraCharacteristics(@NonNull String cameraId) {
        CameraCharacteristics output = null;
        try {
            output = mCameraManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException ex) {
            Timber.e(ex, "Couldn't getCameraCharacteristics of camera: %s", cameraId);
        }

        return output;
    }

    @RequiresPermission(android.Manifest.permission.CAMERA)
    public void openCamera(@NonNull String cameraId,
                           @NonNull final CameraDevice.StateCallback callback,
                           @Nullable Handler handler)
            throws CameraAccessException {

        mCameraManager.openCamera(cameraId, callback, handler);
    }

    public int getAvailableCamerasCount() {
        int output = 0;
        try {
            String[] cameras = getCameraIdList();
            output = cameras.length;
        } catch (CameraAccessException ex) {
            Timber.e(ex, "Couldn't camerasAvailable.");
        }
        return output;
    }

    public String getInitialCameraId() {
        String output = null;
        try {
            String[] cameras = mCameraManager.getCameraIdList();
            if (cameras.length > 0) {
                // get first available
                output = cameras[0];
            }
        } catch (CameraAccessException ex) {
            Timber.e(ex, "Couldn't getInitialCameraId.");
        }
        return output;
    }

    public String getNextCameraId(String cameraId) {
        // use current cameraId if somethings goes wrong or we don't have other camera.
        String output = cameraId;
        try {
            String[] cameras = mCameraManager.getCameraIdList();

            // calc current index
            int currentIndex = 0;
            for (int i = 0; i < cameras.length; i++) {
                if (cameras[i].equals(cameraId)) {
                    currentIndex = i;
                    break;
                }
            }

            // calc next index
            int outputIndex = 0;
            if (currentIndex + 1 < cameras.length) {
                outputIndex = currentIndex + 1;
            }
            output = cameras[outputIndex];

        } catch (CameraAccessException ex) {
            Timber.e(ex, "Couldn't switch to next camera of cId: %s", cameraId);
        }

        return output;
    }

    public static void cloneBuilder(CaptureRequest.Builder in, CaptureRequest.Builder out) {
        out.set(CaptureRequest.CONTROL_MODE, in.get(CaptureRequest.CONTROL_MODE));
        out.set(CaptureRequest.CONTROL_AF_MODE, in.get(CaptureRequest.CONTROL_AF_MODE));
        out.set(CaptureRequest.COLOR_CORRECTION_MODE, in.get(CaptureRequest.COLOR_CORRECTION_MODE));
        out.set(CaptureRequest.CONTROL_EFFECT_MODE, in.get(CaptureRequest.CONTROL_EFFECT_MODE));
        out.set(CaptureRequest.CONTROL_AE_MODE, in.get(CaptureRequest.CONTROL_AE_MODE));
        out.set(CaptureRequest.FLASH_MODE, in.get(CaptureRequest.FLASH_MODE));
    }

}
