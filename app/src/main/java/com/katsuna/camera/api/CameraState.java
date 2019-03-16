package com.katsuna.camera.api;

public enum CameraState {
    /**
     * Camera state: Showing camera preview.
     */
    PREVIEW,
    /**
     * Camera state: Waiting for the focus to be locked.
     */
    WAITING_LOCK,
    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    WAITING_PRECAPTURE,
    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    WAITING_NON_PRECAPTURE,
    /**
     * Camera state: Picture was taken.
     */
    TAKEN
}