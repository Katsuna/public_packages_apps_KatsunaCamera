package com.katsuna.camera.data;

public enum FlashMode {
    ON, OFF, AUTO;

    public static FlashMode toEnum(String flashModeStr) {
        try {
            return valueOf(flashModeStr);
        } catch (Exception ex) {
            // For error cases
            return defaultMode();
        }
    }

    public static FlashMode defaultMode() {
        return OFF;
    }

}
